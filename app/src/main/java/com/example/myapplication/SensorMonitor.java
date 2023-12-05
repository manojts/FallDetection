package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class SensorMonitor extends AppCompatActivity implements SensorEventListener {
    SensorManager manager;
    Sensor acclerometerSensor;
    Sensor gyroScopeSensor;
    TextView AccelerometerX, AccelerometerY, AccelerometerZ,AccelerometerAmpltd;
    TextView GyroscopeX, GyroscopeY, GyroscopeZ, GyroscopeAmpltd;
    Button button;
    Double AccelerometerAmplitude, GyroscopeAmplitude;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_monitor);
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acclerometerSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroScopeSensor = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        button = findViewById(R.id.back);
        String action = getIntent().getAction();
        if ("Start Monitor".equals(action)) {
            System.out.println("Starting monitor");
            // Handle purpose A
        } else if ("Stop Monitor".equals(action)) {
            System.out.println("Stopping monitor");
            manager.unregisterListener(this);
            acclerometerSensor = null;
            gyroScopeSensor = null;
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
            Toast.makeText(getApplicationContext(), "Stopped Sensor Monitor.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (acclerometerSensor != null) {
            manager.registerListener(this, acclerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (gyroScopeSensor != null) {
            manager.registerListener(this, gyroScopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        AccelerometerX = (TextView) findViewById(R.id.AccelerometerX);
        AccelerometerY = (TextView) findViewById(R.id.AccelerometerY);
        AccelerometerZ = (TextView) findViewById(R.id.AccelerometerZ);
        AccelerometerAmpltd = (TextView) findViewById(R.id.AccelerometerAmpltd);
        GyroscopeX = (TextView) findViewById(R.id.GyroscopeX);
        GyroscopeY = (TextView) findViewById(R.id.GyroscopeY);
        GyroscopeZ = (TextView) findViewById(R.id.GyroscopeZ);
        GyroscopeAmpltd = (TextView) findViewById(R.id.GyroscopeAmpltd);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        if(Sensor.TYPE_ACCELEROMETER == sensor.getType()){
            AccelerometerX.setText("X: "+sensorEvent.values[0]);
            AccelerometerY.setText("Y: "+sensorEvent.values[1]);
            AccelerometerZ.setText("Z: "+sensorEvent.values[2]);
            AccelerometerAmplitude = Math.pow(sensorEvent.values[0],2) + Math.pow(sensorEvent.values[1],2)
                    + Math.pow(sensorEvent.values[2],2);
            AccelerometerAmplitude = Math.sqrt(AccelerometerAmplitude);
            AccelerometerAmpltd.setText("Accelerometer Amplitude "+ AccelerometerAmplitude);
        }
        else if(Sensor.TYPE_GYROSCOPE == sensor.getType()){
            GyroscopeX.setText("X: "+sensorEvent.values[0]);
            GyroscopeY.setText("Y: "+sensorEvent.values[1]);
            GyroscopeZ.setText("Z: "+sensorEvent.values[2]);
            GyroscopeAmplitude = Math.pow(sensorEvent.values[0],2) + Math.pow(sensorEvent.values[1],2) +
                    Math.pow(sensorEvent.values[2],2);
            GyroscopeAmplitude = Math.sqrt(GyroscopeAmplitude);
            GyroscopeAmpltd.setText("Gyroscope Amplitude "+ GyroscopeAmplitude);
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


}