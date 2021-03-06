package com.example.nearbytestproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    final int LOCATION_ID = 44;
    final int EXTERNAL_STORAGE_ID = 55;
    FusedLocationProviderClient mFusedLocationClient;
    TextView latTextView, lonTextView , lastLatitude, lastLongitude, distance;
    Location lastLocation;
    Location mLastLocation;
    ToggleButton toggleButton;
    String SERVICE_ID = "com.gothinklearning.example.nearbytestproject";
    boolean nearbyStatus = false;
    String connectedEndpointID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastLatitude = findViewById(R.id.lastLatTextView);
        lastLongitude = findViewById(R.id.lastLongTextView);
        latTextView = findViewById(R.id.latTextView);
        lonTextView = findViewById(R.id.lonTextView);
        distance = findViewById(R.id.distanceTextView);

        toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (checkStoragePermission()) {
                        startAdvertising();
                        startDiscovery();
                    } else {
                        requestStoragePermission();
                        toggleButton.setChecked(false);
                    }
                } else {
                    stopAdvertising();
                    stopDiscovery();
                }
            }
        });

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
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onLocationResult(LocationResult locationResult) {

            mLastLocation = locationResult.getLastLocation();
            //ToDo this should be split into a string and a value in the dsiplay, ie 2 UI elements, not a long string!
            latTextView.setText( getString( R.string.latitude ) + String.valueOf( mLastLocation.getLatitude() ) );
            lonTextView.setText( getString( R.string.longitude ) + String.valueOf( mLastLocation.getLongitude() ) );

            sendData(makeByteArray(mLastLocation));

            lastLocation = mLastLocation;
            lastLatitude.setText( getString( R.string.latitude ) + String.valueOf( lastLocation.getLatitude() ) );
            lastLongitude.setText( getString( R.string.longitude ) + String.valueOf( lastLocation.getLongitude() ) );
        }
    };

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        private byte[] makeByteArray(Location location) {

            String dataToSend = String.valueOf( location.getLatitude()) + "," + String.valueOf(location.getLongitude());
            Log.i("data to send", dataToSend);
            byte [] bytes = dataToSend.getBytes( StandardCharsets.UTF_8);

            return bytes;
        }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private boolean checkStoragePermission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestStoragePermission() {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_ID);
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
                LOCATION_ID
        );
    }

    @Override
    public void onResume(){
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_ID: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                }
            }
            case EXTERNAL_STORAGE_ID: {

                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Please allow access to files", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private void startAdvertising(){
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
        Nearby.getConnectionsClient(this)
                .startAdvertising(
                        getUserNickname(), SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            Log.i("we're advertsing", "send out the ad");
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // We were unable to start advertising.
                        });
    }

    private void startDiscovery() {
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
        Context context = getApplicationContext();
        Nearby.getConnectionsClient(context)
                .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            Log.i("we're discovering", "looking for a signal");
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // We're unable to start discovering.
                        });
    }

    private String getUserNickname () {
        String deviceName = Build.BRAND + " " + Build.DEVICE;
        Log.i("device name", deviceName);
        return deviceName;
    };


    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    // An endpoint was found. We request a connection to it.
                    Context context = getApplicationContext();
                    Nearby.getConnectionsClient(context)
                            .requestConnection(getUserNickname(), endpointId, connectionLifecycleCallback)
                            .addOnSuccessListener(
                                    (Void unused) -> {
                                        // We successfully requested a connection. Now both sides
                                        // must accept before the connection is established.
                                        Log.i("request con", "request connection");
                                    })
                            .addOnFailureListener(
                                    (Exception e) -> {
                                        // Nearby Connections failed to request the connection.
                                        Log.i("failed ", "request connection failed");
                                    });
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // A previously discovered endpoint has gone away.
                }
            };

    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    Context context = getApplicationContext();
                    Nearby.getConnectionsClient( context ).acceptConnection( endpointId, payloadCallback );
                }

                    /*
                    This builds in authentication of the device - whihc we can leave for the moment
                    new AlertDialog.Builder(context)
                            .setTitle("Accept connection to " + info.getEndpointName())
                            .setMessage("Confirm the code matches on both devices: " + info.getAuthenticationToken())
                            .setPositiveButton(
                                    "Accept",
                                    (DialogInterface dialog, int which) ->
                                            // The user confirmed, so we can accept the connection.
                                            Nearby.getConnectionsClient(context)
                                                    .acceptConnection(endpointId, payloadCallback))
                            .setNegativeButton(
                                    android.R.string.cancel,
                                    (DialogInterface dialog, int which) ->
                                            // The user canceled, so we should reject the connection.
                                            Nearby.getConnectionsClient(context).rejectConnection(endpointId))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                */

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            // We're connected! Can now start sending and receiving data.
                            //**** Its here we should be generating and sending the location data

                            nearbyStatus = true;
                            connectedEndpointID = endpointId;

                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // The connection broke before it was able to be accepted.
                            break;
                        default:
                            // Unknown status code
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                }
            };


    private void sendData(byte [] dataToSend) {

        if (nearbyStatus) {
            Payload bytesPayload = Payload.fromBytes( dataToSend );
            Context context = getApplicationContext();
            Nearby.getConnectionsClient(context).sendPayload(connectedEndpointID, bytesPayload);
            Log.i( "sending some data", connectedEndpointID );
            Log.i("data to send in Bytes", Arrays.toString(dataToSend));
        }



    };


    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
           byte[] receivedBytes = payload.asBytes();
            String receivedData = new String(receivedBytes, StandardCharsets.UTF_8);
            String[] splitted = receivedData.split(",");
            //Log.i("latitude", splitted[0]);
            //Log.i("longitude", splitted[1]);
            Double lat = Double.valueOf( splitted[0]);
            Double lon = Double.valueOf( splitted[1]);

            Log.i("received lat", String.valueOf(lat));
            Log.i("received long", String.valueOf(lon));
            Log.i("this lat", String.valueOf(mLastLocation.getLatitude()));
            Log.i("this long", String.valueOf(mLastLocation.getLongitude()));


            float[] results = new float[3];
            Location.distanceBetween( mLastLocation.getLatitude(), mLastLocation.getLongitude(), lat, lon, results );
            distance.setText( getString( R.string.distance ) + String.valueOf( results[0] ) );



        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
            //
        }
    };












    private void stopAdvertising(){
        //Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
        //todo
    }


    private void stopDiscovery(){
        //Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
        //todo
    }

}
















