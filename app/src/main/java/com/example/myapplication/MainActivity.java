package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private SensorManager sensorManager;
    private Sensor accelerometerSensor, gyroscopeSensor;
    private static final float FALL_THRESHOLD = 25.0f;
    private static final float GYROSCOPE_THRESHOLD = 5.0f;
    private static final long POST_FALL_ACTIVITY_DELAY = 2000; // 2 seconds
    private boolean potentialFallDetected = false;
    private boolean significantRotationDetected = false;
    private long fallTime = 0;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;

    private Button buttonStartMonitor, buttonStopMonitor;
    private ProgressBar progressBar;

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 2;

    private boolean monitoringStarted = false;
    private float ax = 5.0f;
    private  float ay = 5.0f;
    private float az = 5.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("UserData");

        buttonStartMonitor = findViewById(R.id.start_monitor);
        buttonStopMonitor = findViewById(R.id.stop_monitor);
        progressBar = findViewById(R.id.progressBar);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        buttonStartMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndStartMonitor();
            }
        });

        buttonStopMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopMonitor();
            }
        });

        // Menu bar click
        ImageView menu_bar = findViewById(R.id.menu_bar);
        menu_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });
    }

    private void checkAndStartMonitor() {
        if (checkSmsPermission() && checkLocationPermission()) {
            if (isLocationEnabled()) {
                startMonitor();
            } else {
                // Location setting is disabled, ask the user to enable it
                requestLocationSetting();
            }
        } else {
            requestPermissions();
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void requestLocationSetting() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }


    private boolean checkSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkLocationSettings() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermissions() {
        // Request SMS permission
        if (!checkSmsPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        }

        // Request Location permission
        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(MainActivity.this, view);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.profile) {
                    // Add code to open the profile edit activity
                    Intent intent = new Intent(getApplicationContext(), Profile.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.logout) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), LoginUser.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });
        popup.show();
    }

    private void startMonitor() {

        buttonStartMonitor.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        potentialFallDetected = false;
        significantRotationDetected = false;

        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);

        Toast.makeText(this, "Monitoring started", Toast.LENGTH_SHORT).show();

        monitoringStarted = true;

    }

    private void stopMonitor() {

        buttonStartMonitor.setEnabled(true);

        progressBar.setVisibility(View.GONE);


        sensorManager.unregisterListener(this);
        Toast.makeText(this, "Monitoring stopped", Toast.LENGTH_SHORT).show();

        monitoringStarted = false;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            handleAccelerometerData(event);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            handleGyroscopeData(event);
        }
    }

    private void handleAccelerometerData(SensorEvent event) {
        float SMV = calculateSMV(event.values);

        if (SMV > FALL_THRESHOLD) {
            potentialFallDetected = true;
            fallTime = System.currentTimeMillis();
        }
        else if(Math.abs(event.values[0] - ax)>10){
            potentialFallDetected = true;
            fallTime = System.currentTimeMillis();
        }
        else if(Math.abs(event.values[1] - ay)>10){
            potentialFallDetected = true;
            fallTime = System.currentTimeMillis();
        }
        else if(Math.abs(event.values[2] - az)>10){
            potentialFallDetected = true;
            fallTime = System.currentTimeMillis();
        }
        ax = event.values[0];
        ay = event.values[1];
        az = event.values[2];
        if (potentialFallDetected && (System.currentTimeMillis() - fallTime > POST_FALL_ACTIVITY_DELAY)) {
            if (significantRotationDetected) {
                onFallDetected();
                potentialFallDetected = false;
                significantRotationDetected = false;
            }
        }
    }

    private void handleGyroscopeData(SensorEvent event) {
        float rotationMagnitude = calculateRotationMagnitude(event.values);

        if (rotationMagnitude > GYROSCOPE_THRESHOLD) {
            significantRotationDetected = true;
        }
    }

    private float calculateSMV(float[] values) {
        return (float) Math.sqrt(Math.pow(values[0], 2) + Math.pow(values[1], 2) + Math.pow(values[2], 2));
    }

    private float calculateRotationMagnitude(float[] gyroValues) {
        return (float) Math.sqrt(Math.pow(gyroValues[0], 2) + Math.pow(gyroValues[1], 2) + Math.pow(gyroValues[2], 2));
    }

    private void onFallDetected() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserData");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            Query query = databaseReference.orderByChild("email").equalTo(currentUser.getEmail());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String emergencyContactNumber = snapshot.child("getEmergencyContactNumber").getValue(String.class);
                        String fallenPersonName = snapshot.child("userName").getValue(String.class);

                        if (emergencyContactNumber != null && !emergencyContactNumber.isEmpty() && fallenPersonName != null && !fallenPersonName.isEmpty()) {
                            if (checkLocationPermission()) {
                                // Obtain the current location and handle it asynchronously
                                getCurrentLocation(new LocationCallback() {
                                    @Override
                                    public void onLocationObtained(String location) {
                                        // Create the SMS message with the location
                                        String smsMessage = "Fall detected! " + fallenPersonName + " has fallen. Location: " + location;

                                        // Send the SMS
                                        sendSMS(emergencyContactNumber, smsMessage);
                                    }
                                });
                            } else {
                                // Request location permissions
                                requestLocationPermission();
                            }

                        } else {
                            // Handle case where no emergency contact or fallen person name is available
                            Toast.makeText(MainActivity.this, "No emergency contact or fallen person name available", Toast.LENGTH_SHORT).show();
                        }
                        break; // Assuming each user has one unique entry
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle database error
                }
            });
        }
    }

    private void requestLocationPermission() {
        // Request location permissions
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
    }

    private void getCurrentLocation(final LocationCallback callback) {
        // Initialize the LocationManager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check for location provider availability
        if (locationManager != null && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {

            if (checkLocationPermission()) {

                // Request a single location update
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        // Handle the location change
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        // Create a Google Maps link
                        String googleMapsLink = "https://maps.google.com/maps?q=" + latitude + "," + longitude;

                        // Return the location information
                        String locationInfo = "Latitude: " + latitude + ", Longitude: " + longitude + "\nGoogle Maps link: " + googleMapsLink;

                        // Execute the callback with the location information
                        callback.onLocationObtained(locationInfo);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        // Handle status changes if needed
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        // Handle provider enabled
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        // Handle provider disabled
                    }
                }, null);
            }

        } else {
            // Location provider is not available
            Toast.makeText(MainActivity.this, "Location provider is not available", Toast.LENGTH_SHORT).show();
            callback.onLocationObtained("Location not available");
        }
    }

    // Define a callback interface
    private interface LocationCallback {
        void onLocationObtained(String location);
    }


    private void sendSMS(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getApplicationContext(), "Emergency SMS Sent", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // SMS permission granted, you can handle it if needed
                } else {
                    // SMS permission denied
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case MY_PERMISSIONS_REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Location permission granted, you can handle it if needed
                } else {
                    // Location permission denied
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            // Handle other permission requests if needed

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Auto-generated method stub
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
}
