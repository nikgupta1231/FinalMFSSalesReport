package com.myapplication.nik.mfssalesreport;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener,
        View.OnClickListener {

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrentLocationMarker;
    LocationRequest mLocationRequest;
    private FirebaseAuth mAuth;

    DatabaseReference mReference;
    MainDatabaseFields mainDatabaseFields;

    private boolean isBackButtonClicked = false;

    SharedPreferences mSharedPreferences;

    int refNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //drawerLayout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Button working
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);

        //Instance of Realtime Database reference object
        mReference = FirebaseDatabase.getInstance().getReference("UserId/");
        //Main local database object
        mainDatabaseFields = new MainDatabaseFields();

        mSharedPreferences = MapsActivity.this.getPreferences(MODE_PRIVATE);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_signOut) {
            mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            Intent mIntent = new Intent(MapsActivity.this, MainActivity.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(mIntent);
            MapsActivity.this.finish();
        }
//        else if (id == R.id.nav_share) {
//            mActionProvider = (ActionProvider) MenuItemCompat.getActionProvider(item);
//
//        }
        else if (id == R.id.nav_about) {
//            Snackbar mSnackbar = Snackbar.make(item.getActionView(), "Contact me @ guptanikhil1231@gmial.com", BaseTransientBottomBar.LENGTH_SHORT);
//            mSnackbar.show();
            Toast.makeText(this, "Contact me at guptanikhil1231@gmail.com", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_contact) {
//            Snackbar mSnackbar = Snackbar.make(item.getActionView(), "Contact me @ guptanikhil1231@gmial.com", BaseTransientBottomBar.LENGTH_SHORT);
//            mSnackbar.show();
            Toast.makeText(this, "Contact me at guptanikhil1231@gmail.com", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    ) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build())
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;


        if (mCurrentLocationMarker != null) {
            mCurrentLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng)
                .title("Current Location");
        mCurrentLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, Please Enable it!")
                .setCancelable(false)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean checkForMockLocation(Location currLocation) {
        if (Build.VERSION.SDK_INT <= 18) {

            return !(Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"));

        } else {
            return currLocation.isFromMockProvider();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
        Toast.makeText(this, "" + mSharedPreferences.getInt("refNo", refNo), Toast.LENGTH_SHORT).show();
        refNo = mSharedPreferences.getInt("refNo", refNo);

    }

    String startLatitude, stopLatitude, startLongitude, stopLongitude;

    private void startAction() {
        //updating current location
        Location presentLoc = mMap.getMyLocation();
        startLatitude = Double.toString(presentLoc.getLatitude());
        startLongitude = Double.toString(presentLoc.getLongitude());
        mainDatabaseFields.setStartLoaction(startLatitude + "," + startLongitude);

        //updating current time
        Date mDate = Calendar.getInstance().getTime();
        String currDate = mDate.toString();
        mainDatabaseFields.setStartTime(currDate);


        Toast.makeText(this, "" + startLatitude + "," + startLongitude, Toast.LENGTH_SHORT).show();


        findViewById(R.id.start).setVisibility(View.GONE);
        findViewById(R.id.stop).setVisibility(View.VISIBLE);

////        Snackbar mSnackbar = Snackbar.make(findViewById(R.id.start), "Contact me @ guptanikhil1231@gmial.com", BaseTransientBottomBar.LENGTH_INDEFINITE);
////        mSnackbar.setAction(, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//        mSnackbar.show();

    }

    private void stopAction() {
        //updating current location
        Location presentLoc = mMap.getMyLocation();
        stopLatitude = Double.toString(presentLoc.getLatitude());
        stopLongitude = Double.toString(presentLoc.getLongitude());
        mainDatabaseFields.setStopLocaiton(stopLatitude + "," + stopLongitude);

        //updating current time
        Date mDate = Calendar.getInstance().getTime();
        String currDate = mDate.toString();
        mainDatabaseFields.setStopTime(currDate);

        findViewById(R.id.stop).setVisibility(View.GONE);
        findViewById(R.id.start).setVisibility(View.VISIBLE);
        postOnDatabase();
        startActivity(new Intent(MapsActivity.this, Main2Activity.class).putExtra("refNo", refNo));

    }

    private void postOnDatabase() {

        SharedPreferences.Editor editor = mSharedPreferences.edit();

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference newReference = mReference.child(mAuth.getCurrentUser().getUid()).child(Integer.toString(refNo)).getRef();
        refNo++;
        newReference.child("startLocation").setValue(mainDatabaseFields.getStartLoaction());
        newReference.child("startTime").setValue(mainDatabaseFields.getStartTime());
        newReference.child("stopLocation").setValue(mainDatabaseFields.getStopLocaiton());
        newReference.child("stopTime").setValue(mainDatabaseFields.getStopTime());

        editor.putInt("refNo", refNo);
        editor.apply();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.start) {
            startAction();
            Toast.makeText(this, "start pressed", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.stop) {
            stopAction();
            Toast.makeText(this, "stop pressed", Toast.LENGTH_SHORT).show();
        }
    }


    //    end of application exit-point
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


    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

}
