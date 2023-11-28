package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {
    TextInputEditText textEmail, textPassword, username, emergencyContactName, emergencyContactNumber;
    Button button;
    String email, userName, emergency_ContactName, emergency_ContactNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        textEmail = findViewById(R.id.email);
        username = findViewById(R.id.username);
        emergencyContactName = findViewById(R.id.emergencyContactName);
        emergencyContactNumber = findViewById(R.id.emergencyContactNumber);
        button = findViewById(R.id.update_profile);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserData");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        Query query = databaseReference.orderByChild("email").equalTo(currentUser.getEmail());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    userName = snapshot.child("userName").getValue(String.class);
                    emergency_ContactName = snapshot.child("emergencyContactName").getValue(String.class);
                    emergency_ContactNumber = snapshot.child("getEmergencyContactNumber").getValue(String.class);
                    email = snapshot.child("email").getValue(String.class);
                    System.out.println(userName);
                    System.out.println(email);
                    System.out.println(emergency_ContactName);
                    System.out.println(emergency_ContactNumber);
                    textEmail.setText(email);
                    username.setText(userName);
                    emergencyContactName.setText(emergency_ContactName);
                    emergencyContactNumber.setText(emergency_ContactNumber);
                    break;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
        textEmail.setText(email);
        username.setText(userName);
        emergencyContactName.setText(emergency_ContactName);
        emergencyContactNumber.setText(emergency_ContactNumber);

    }
}