package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity {
    TextInputEditText textEmail, textPassword, username, emergencyContactName, emergencyContactNumber;
    Button button;
    FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference reference;
    ProgressBar progressbar;
    TextView textView;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        textEmail = findViewById(R.id.email);
        textPassword = findViewById(R.id.password);
        username = findViewById(R.id.username);
        emergencyContactName = findViewById(R.id.emergencyContactName);
        emergencyContactNumber = findViewById(R.id.emergencyContactNumber);
        button = findViewById(R.id.registerButton);
        progressbar = findViewById(R.id.progressbar);
        textView = findViewById(R.id.loginNow);

        mAuth = FirebaseAuth.getInstance();

        // Hide password characters with dots
        textPassword.setTransformationMethod(new PasswordTransformationMethod());

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginUser.class);
                startActivity(intent);
                finish();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve input values
                String email = textEmail.getText().toString().trim();
                String password = textPassword.getText().toString().trim();
                String userName = username.getText().toString().trim();
                String emergency_ContactName = emergencyContactName.getText().toString().trim();
                String emergency_ContactNumber = emergencyContactNumber.getText().toString().trim();

                progressbar.setVisibility(View.VISIBLE);

                if (!isValidUserName(userName)) {
                    Toast.makeText(RegisterUser.this, "Invalid User Name. Only alphabet letters are allowed.", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                    return;
                }

                if (!isValidUserName(emergency_ContactName)) {
                    Toast.makeText(RegisterUser.this, "Invalid Emergency Contact Name. Only alphabet letters are allowed.", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                    return;
                }

                // Validate input fields
                if (TextUtils.isEmpty(email) || !isValidEmail(email)) {
                    Toast.makeText(RegisterUser.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(password) || !isValidPassword(password)) {
                    Toast.makeText(RegisterUser.this, "Invalid password format", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(userName)) {
                    Toast.makeText(RegisterUser.this, "User Name cannot be empty", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(emergency_ContactName)) {
                    Toast.makeText(RegisterUser.this, "Emergency Contact Name cannot be empty", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(emergency_ContactNumber) || !isValidPhoneNumber(emergency_ContactNumber)) {
                    Toast.makeText(RegisterUser.this, "Invalid Emergency Contact Number", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                    return;
                }

                // Create user with email and password
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String uid = task.getResult().getUser().getUid();
                                    User user = new User(userName, email, emergency_ContactName, emergency_ContactNumber);
                                    user.setUserId(uid);
                                    storeUserData(uid, user);

                                    Toast.makeText(RegisterUser.this, "Account created.", Toast.LENGTH_SHORT).show();
                                    progressbar.setVisibility(View.GONE);

                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    // If sign up fails, check if the failure is due to duplicate email
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        Toast.makeText(RegisterUser.this, "Email address is already in use.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(RegisterUser.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                    progressbar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), LoginUser.class);
        startActivity(intent);
        finish();
    }

    private boolean isValidUserName(String userName) {
        return userName.matches("^[a-zA-Z ]+$");
    }

    // Method to validate email format
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Method to validate password format
    private boolean isValidPassword(String password) {
        // Password format: At least one capital character, one special character, one number, one lowercase letter, and min length 9
        String passwordPattern = "^(?=.*[A-Z])(?=.*[@#$%^&+=])(?=.*[0-9])(?=.*[a-z]).{9,}$";
        return password.matches(passwordPattern);
    }

    // Method to validate the format of the phone number (exactly 10 digits)
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{10}");
    }

    public void storeUserData(String uid, User user) {
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("UserData");

        reference.child(uid).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterUser.this, "Account created.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterUser.this, "Unable to Create Account.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
