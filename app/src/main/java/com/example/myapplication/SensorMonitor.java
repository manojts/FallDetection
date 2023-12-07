package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class SensorMonitor extends Activity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometerSensor, gyroscopeSensor;
    private TextView AccelerometerX, AccelerometerY, AccelerometerZ, AccelerometerAmpltd;
    private TextView GyroscopeX, GyroscopeY, GyroscopeZ, GyroscopeAmpltd;
    private static final float FALL_THRESHOLD = 25.0f;
    private static final float GYROSCOPE_THRESHOLD = 5.0f;
    private static final long POST_FALL_ACTIVITY_DELAY = 2000; // 2 seconds
    private boolean potentialFallDetected = false;
    private boolean significantRotationDetected = false;
    private long fallTime = 0;

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_monitor);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);

        AccelerometerX = (TextView) findViewById(R.id.AccelerometerX);
        AccelerometerY = (TextView) findViewById(R.id.AccelerometerY);
        AccelerometerZ = (TextView) findViewById(R.id.AccelerometerZ);
        AccelerometerAmpltd = (TextView) findViewById(R.id.AccelerometerAmpltd);
        GyroscopeX = (TextView) findViewById(R.id.GyroscopeX);
        GyroscopeY = (TextView) findViewById(R.id.GyroscopeY);
        GyroscopeZ = (TextView) findViewById(R.id.GyroscopeZ);
        GyroscopeAmpltd = (TextView) findViewById(R.id.GyroscopeAmpltd);
        // Check for SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        }

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
        AccelerometerX.setText("X: " + event.values[0]);
        AccelerometerY.setText("Y: " + event.values[1]);
        AccelerometerZ.setText("Z: " + event.values[2]);

        float SMV = calculateSMV(event.values);
        AccelerometerAmpltd.setText("SMV: " + SMV);

        if (SMV > FALL_THRESHOLD) {
            potentialFallDetected = true;
            fallTime = System.currentTimeMillis();
        }

        if (potentialFallDetected && (System.currentTimeMillis() - fallTime > POST_FALL_ACTIVITY_DELAY)) {
            if (significantRotationDetected) {
                onFallDetected();
                potentialFallDetected = false;
                significantRotationDetected = false;
            }
        }
    }

    private void handleGyroscopeData(SensorEvent event) {
        GyroscopeX.setText("X: " + event.values[0]);
        GyroscopeY.setText("Y: " + event.values[1]);
        GyroscopeZ.setText("Z: " + event.values[2]);

        float rotationMagnitude = calculateRotationMagnitude(event.values);
        GyroscopeAmpltd.setText("Magnitude: " + rotationMagnitude);

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
                        if (emergencyContactNumber != null && !emergencyContactNumber.isEmpty()) {
                            sendSMS(emergencyContactNumber, "Fall detected! This is an automatic message from the Sensor Monitor app.");
                        } else {
                            // Handle case where no emergency contact is available
                            Toast.makeText(SensorMonitor.this, "No emergency contact number available", Toast.LENGTH_SHORT).show();
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
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "SMS permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "SMS permission denied", Toast.LENGTH_LONG).show();
            }
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
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
