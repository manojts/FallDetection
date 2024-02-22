package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    TextInputEditText textEmail, username, emergencyContactName, emergencyContactNumber;
    Button button;
    String email, userName, emergencyContactNameStr, emergencyContactNumberStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        textEmail = findViewById(R.id.email);
        username = findViewById(R.id.username);
        emergencyContactName = findViewById(R.id.emergencyContactName);
        emergencyContactNumber = findViewById(R.id.emergencyContactNumber);
        button = findViewById(R.id.update_profile);

        // Disable the email field for editing
        textEmail.setEnabled(false);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserData");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            Query query = databaseReference.orderByChild("email").equalTo(currentUser.getEmail());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Retrieve user data from the database
                        userName = snapshot.child("userName").getValue(String.class);
                        emergencyContactNameStr = snapshot.child("emergencyContactName").getValue(String.class);
                        emergencyContactNumberStr = snapshot.child("getEmergencyContactNumber").getValue(String.class);

                        // Set the retrieved data to the corresponding EditText fields
                        textEmail.setText(currentUser.getEmail());
                        username.setText(userName);
                        emergencyContactName.setText(emergencyContactNameStr);
                        emergencyContactNumber.setText(emergencyContactNumberStr);

                        break; // Assuming each user has one unique entry
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle database error
                }
            });
        }

        // Set up the update button click listener
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
    }

    // Method to handle updating user profile
    private void updateProfile() {
        // Validate the input fields
        if (validateInputs()) {
            // Update the user data in the database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserData");
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                Query query = databaseReference.orderByChild("email").equalTo(currentUser.getEmail());

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Update the user data
                            snapshot.getRef().child("userName").setValue(userName);
                            snapshot.getRef().child("emergencyContactName").setValue(emergencyContactNameStr);
                            snapshot.getRef().child("getEmergencyContactNumber").setValue(emergencyContactNumberStr);

                            // Inform the user about the successful update
                            Toast.makeText(Profile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                            // Redirect back to MainActivity
                            onBackPressed();

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
    }

    // Method to validate user input fields
    private boolean validateInputs() {
        // Retrieve the input values
        userName = username.getText().toString().trim();
        emergencyContactNameStr = emergencyContactName.getText().toString().trim();
        emergencyContactNumberStr = emergencyContactNumber.getText().toString().trim();

        // Check if any of the fields is empty
        if (userName.isEmpty() || emergencyContactNameStr.isEmpty() || emergencyContactNumberStr.isEmpty()) {
            // Display an error message or toast for empty fields
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if the emergency contact number has exactly 10 digits
        if (!isValidPhoneNumber(emergencyContactNumberStr)) {
            // Display an error message or toast for an invalid phone number format
            Toast.makeText(this, "Invalid phone number format. Please enter a 10-digit number", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Add more specific validation logic as needed

        return true;
    }

    // Method to validate the format of the phone number (exactly 10 digits)
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{10}");
    }

    // Override onBackPressed to redirect to MainActivity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(Profile.this, MainActivity.class));
        finish();
    }
}