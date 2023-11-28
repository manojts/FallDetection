package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginUser.class);
                startActivity(intent);
                finish();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email;
                String password;
                String userName;
                String emergency_ContactName ;
                String emergency_ContactNumber;
                email = String.valueOf(textEmail.getText());
                password= String.valueOf(textPassword.getText());
                userName = String.valueOf(username.getText());
                emergency_ContactName= String.valueOf(emergencyContactName.getText());
                emergency_ContactNumber = String.valueOf(emergencyContactNumber.getText());
                progressbar.setVisibility(View.VISIBLE);
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(RegisterUser.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterUser.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(userName)){
                    Toast.makeText(RegisterUser.this, "User Name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(emergency_ContactName)){
                    Toast.makeText(RegisterUser.this, "Emergency Contact Name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(emergency_ContactNumber)){
                    Toast.makeText(RegisterUser.this, "Emergency Contact Number cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
//                                    FirebaseUser user = mAuth.getCurrentUser();
                                    String uid = task.getResult().getUser().getUid();
                                    User user = new User(userName, email, emergency_ContactName, emergency_ContactNumber);
                                    user.setUserId(uid);
                                    storeUserData(uid, user);
//                                    System.out.println(uid);
//                                    System.out.println(user.toString());
//                                    db = FirebaseDatabase.getInstance();
//                                    reference = db.getReference("UserData");
//                                    reference.child(uid).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()) {
//                                                Toast.makeText(RegisterUser.this, "Account created.",
//                                                        Toast.LENGTH_SHORT).show();
//                                            } else {
//                                                Toast.makeText(RegisterUser.this, "Unable to Create Account.",
//                                                        Toast.LENGTH_SHORT).show();
//                                            }
//                                        }
//                                    });


                                    Toast.makeText(RegisterUser.this, "Account created.",
                                            Toast.LENGTH_SHORT).show();
                                    progressbar.setVisibility(View.GONE);
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(RegisterUser.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    progressbar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });
    }
    public void storeUserData(String uid, User user) {
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("UserData");
        Log.d("storeUserData", db.toString());
        Log.d("storeUserData", String.valueOf(reference));
        reference.child(uid).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterUser.this, "Account created.",
                                                       Toast.LENGTH_SHORT).show();
                    // Data successfully written to the database
                } else {
                    Toast.makeText(RegisterUser.this, "Unable to Create Account.",
                                                        Toast.LENGTH_SHORT).show();
                    // If writing fails, handle the error
                    // task.getException().getMessage() contains the error message
                }
            }
        });
    }
}