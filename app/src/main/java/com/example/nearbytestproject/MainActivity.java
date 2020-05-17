package com.example.nearbytestproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;

//TODO Add:
//      - Use android only Connections API using peer2peer strategy
//      - responsive UI for lanscape and portrait
//      - DONE is the app using FINE or COARSE: Ensure Location is forced to use FINE - it is!!

public class MainActivity extends AppCompatActivity {

    int MYLOCATION = 44;
    int EXTERNAL_STORAGE = 55;
    FusedLocationProviderClient mFusedLocationClient;
    TextView latTextView, lonTextView , lastLatitude, lastLongitude, distance;
    Location lastLocation;
    Button scanButton;
    Boolean isActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanButton = findViewById(R.id.scanButton);
        lastLatitude = findViewById(R.id.lastLatTextView);
        lastLongitude = findViewById(R.id.lastLongTextView);
        latTextView = findViewById(R.id.latTextView);
        lonTextView = findViewById(R.id.lonTextView);
        distance = findViewById(R.id.distanceTextView);


        scanButton.setOnClickListener(new View.OnClickListener() {
                                          public void onClick(View v) {
                                                startScanningForNearbyDevices();
                                               }});

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLastLocation();
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                lastLocation = task.getResult();
                                if (lastLocation == null) {
                                    requestNewLocationData();
                                } else {
                                    lastLatitude.setText(getString(R.string.latitude) + String.valueOf(lastLocation.getLatitude()));
                                    lastLongitude.setText(getString(R.string.longitude) + String.valueOf(lastLocation.getLongitude()));
                                    requestNewLocationData();
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //location updates accurate to < 1m
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        //mLocationRequest.setNumUpdates(1);
        mLocationRequest.setExpirationDuration(120000); //2 mins

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            Location mLastLocation = locationResult.getLastLocation();
            latTextView.setText(getString(R.string.latitude) + String.valueOf(mLastLocation.getLatitude()));
            lonTextView.setText(getString(R.string.longitude) + String.valueOf(mLastLocation.getLongitude()));

            float [] results = new float[3];
            Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), mLastLocation.getLatitude(), mLastLocation.getLongitude(), results);
            distance.setText(getString(R.string.distance) + String.valueOf(results[0]));

            lastLocation = mLastLocation;
            lastLatitude.setText(getString(R.string.latitude) + String.valueOf(lastLocation.getLatitude()));
            lastLongitude.setText(getString(R.string.longitude) + String.valueOf(lastLocation.getLongitude()));

            Log.i("results array", Arrays.toString(results));
        }
    };

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                MYLOCATION
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MYLOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }

        if (requestCode == EXTERNAL_STORAGE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.i("lets start", "sending and receiving ");

            } else {
                Toast.makeText(this, "Please allow access to files", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }

    }

    private void startScanningForNearbyDevices(){

        if (checkNearbyPermissions()) {
            isActive = !isActive;
            //get started with send and recieve lat and longitude
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE);
        };

        }

    private boolean checkNearbyPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i("external storage", "permission not yet granted!");
                return false;
        }
            else {
                return true;
        }
    }






}
















