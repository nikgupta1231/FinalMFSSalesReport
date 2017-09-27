package com.myapplication.nik.mfssalesreport;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {

    TextInputEditText medit, mcustomerName, mcustomerLocation, mcustomerNumber, memailId, mDepartment, mDiscussion, mActionPlan, mRemarks;
    String customerName, customerLocation, customerNumber, emailId, department, discussion, actionPlan, remark;
    int mYear, mMonth, mDay;
    int refNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mcustomerName = (TextInputEditText) findViewById(R.id.custName);
        mcustomerLocation = (TextInputEditText) findViewById(R.id.custLocation);
        mcustomerNumber = (TextInputEditText) findViewById(R.id.custNumber);
        memailId = (TextInputEditText) findViewById(R.id.custEmail);
        mDepartment = (TextInputEditText) findViewById(R.id.custDepartment);
        mDiscussion = (TextInputEditText) findViewById(R.id.discussion);
        mActionPlan = (TextInputEditText) findViewById(R.id.actionPlan);
        mRemarks = (TextInputEditText) findViewById(R.id.agentRemark);
        medit = (TextInputEditText) findViewById(R.id.timeline);

        findViewById(R.id.submit).setOnClickListener(this);
        findViewById(R.id.timeline).setOnClickListener(this);
        refNo = getIntent().getIntExtra("refNo", refNo) - 1;
        Toast.makeText(this, "" + getIntent().getIntExtra("refNo", refNo), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.submit) {
            Toast.makeText(this, " " + refNo, Toast.LENGTH_SHORT).show();

            AlertDialog.Builder mBuilder = new AlertDialog.Builder(Main2Activity.this);
            mBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    postDataToDatabase();
                }
            });
            mBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(Main2Activity.this, "Fill details and submit", Toast.LENGTH_SHORT).show();
                }
            });
            mBuilder.setMessage("Do you wish to continue....!");
            mBuilder.setCancelable(false);
            mBuilder.show();
        }
        if (id == R.id.timeline) {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog mDatePickerDialog = new DatePickerDialog(Main2Activity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    Toast.makeText(Main2Activity.this, "" + year + " " + month + " " + dayOfMonth, Toast.LENGTH_SHORT).show();
                    String date = dayOfMonth + " " + (month + 1) + " " + year;
                    medit.setText(date);
                }
            }, mYear, mMonth, mDay);
            mDatePickerDialog.show();
        }
    }

    private void postDataToDatabase() {
        getData();
        postData();
    }

    private void postData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("/UserId")
                .child(mAuth.getCurrentUser().getUid())
                .child(Integer.toString(refNo));
        mReference.child("customerName").setValue(customerName);
        mReference.child("customerLocation").setValue(customerLocation);
        mReference.child("customerNumber").setValue(customerNumber);
        mReference.child("emailId").setValue(emailId);
        mReference.child("department").setValue(department);
        mReference.child("discussion").setValue(discussion);
        mReference.child("timeLine").setValue(medit.getText().toString());
        Toast.makeText(this, "visit successfully completed", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Main2Activity.this, MapsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
    }

    private void getData() {
        customerName = mcustomerName.getText().toString();
        customerLocation = mcustomerLocation.getText().toString();
        customerNumber = mcustomerNumber.getText().toString();
        emailId = memailId.getText().toString();
        department = mDepartment.getText().toString();
        discussion = mDiscussion.getText().toString();
        actionPlan = mActionPlan.getText().toString();
        remark = mRemarks.getText().toString();
    }


}
