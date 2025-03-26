package com.sohan.connectsphere;

public class User {
    public String userId, name, email, userType;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String userId, String name, String email, String userType) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.userType = userType; // "Student" only at signup
    }
}
