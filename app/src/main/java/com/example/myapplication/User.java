package com.example.myapplication;

import android.provider.ContactsContract;

public class User {
    private  String userId;
    private String userName;
    private String email;
    private String password;
    private String emergencyContactName;
    private String emergencyContactNumber;

    public User(){

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User(String userName, String email, String emergencyContactName, String EmergencyContactNumber) {
        this.userName = userName;
        this.email = email;
        this.emergencyContactName = emergencyContactName;
        this.emergencyContactNumber = EmergencyContactNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getGetEmergencyContactNumber() {
        return emergencyContactNumber;
    }

    public void setGetEmergencyContactNumber(String getEmergencyContactNumber) {
        this.emergencyContactNumber = emergencyContactNumber;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", emergencyContactName='" + emergencyContactName + '\'' +
                ", emergencyContactNumber='" + emergencyContactNumber + '\'' +
                '}';
    }
}
