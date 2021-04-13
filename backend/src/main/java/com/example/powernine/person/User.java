package com.example.powernine.person;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

@Document(collection = "user")
public class User {

    @Id
    private String UID;
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean comparePasswords(String password) {
        return this.password.equals(getPasswordHash(password));
    }

    static String getPasswordHash(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10, new SecureRandom());
        return encoder.encode(password);
    }

    @Override
    public String toString() {
        return getUsername() + " " + getPasswordHash(getPassword());
    }
}
