package com.example.myapplication;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class  MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    Button button;
    Button button_startMonitor;
    Button button_stopMonitor;
    ImageView menu_bar;
    TextView textView;
    FirebaseUser user;
    MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = new MainActivity();
        mAuth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        button_startMonitor = findViewById(R.id.start_monitor);
        button_stopMonitor = findViewById(R.id.stop_monitor);
        menu_bar = findViewById(R.id.menu_bar);
        user = mAuth.getCurrentUser();

        if(user == null){
            Intent intent = new Intent(getApplicationContext(), LoginUser.class);
            startActivity(intent);
            finish();
        }
        else{
            System.out.println(user.getDisplayName());
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginUser.class);
                startActivity(intent);
                finish();
            }
        });
        button_startMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SensorMonitor.class);
                intent.setAction("Start Monitor");
                startActivity(intent);
                finish();
            }
        });
        button_stopMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SensorMonitor.class);
                intent.setAction("Stop Monitor");
                startActivity(intent);
                finish();
            }
        });
        menu_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);

            }
        });



    }
    public void showPopup(View view){
        PopupMenu popup = new PopupMenu(MainActivity.this, view);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.profile){
                    Intent intent = new Intent(getApplicationContext(), Profile.class);
                    startActivity(intent);
                    finish();
                }
                else if(item.getItemId() == R.id.logout){
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
}