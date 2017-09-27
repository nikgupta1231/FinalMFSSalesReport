package com.myapplication.nik.mfssalesreport;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean isBackButtonClicked = false;

    private final String TAG = MainActivity.class.getSimpleName();
    private TextInputEditText mEmailField, mPasswordField;
    private FirebaseAuth mAuth;
    private ProgressDialog mDialog, mProgressDialog;

    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        mEmailField = (TextInputEditText) findViewById(R.id.emailInput);
        mPasswordField = (TextInputEditText) findViewById(R.id.passwordInput);

        // Buttons
        findViewById(R.id.signIn).setOnClickListener(this);
        findViewById(R.id.createNewAccount).setOnClickListener(this);
        findViewById(R.id.forgotPassword).setOnClickListener(this);

        // Authorisation
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount: " + email);
        if (!validateForm()) {
            return;
        }
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Creating Account....");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //Account Created successful
                    mAuth=FirebaseAuth.getInstance();
                    sendEmailVerification();
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference();
                    Toast.makeText(MainActivity.this, "Restart after Email verification", Toast.LENGTH_SHORT).show();
                    //updateUI(user);
                } else {
                    //Authentication failed
                    Toast.makeText(MainActivity.this, "Email id already in use", Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
                mProgressDialog.hide();
            }
        });
    }

    private void signIn(String email, String password) {
        if (!validateForm()) {
            return;
        }

        mDialog = new ProgressDialog(this);
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Signing In...");
        mDialog.setCancelable(false);
        mDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        mDialog.hide();
                        // [END_EXCLUDE]
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void sendEmailVerification() {
        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]

                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(MainActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "password reset e-mail has been sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "re check the e-mail id again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            if (!currentUser.isEmailVerified()) {
                Toast.makeText(this, "please verify email id and try again", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent mintent = new Intent(getApplicationContext(), MapsActivity.class);
            mintent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(mintent);
            MainActivity.this.finish();

        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.createNewAccount) {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());

        } else if (id == R.id.signIn) {
// have to check whether the email is verified or not if not verified then not access to the application
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (id == R.id.forgotPassword) {
            if (mEmailField.getText().toString().isEmpty()) {
                Toast.makeText(this, "Enter email id to reset password", Toast.LENGTH_SHORT).show();
            } else {
                resetPassword(mEmailField.getText().toString());
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (!isBackButtonClicked) {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            isBackButtonClicked = true;
        } else {
            super.onBackPressed();
        }
        new CountDownTimer(3000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                isBackButtonClicked = false;
            }
        }.start();
    }
}
