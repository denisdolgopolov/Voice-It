package com.com.technoparkproject.models;

import java.util.List;

public final class User {
    public final String userName;
    public final List<String> records;
    public final String profileImageUUID;
    public final String userAuthUUID;

    public User(String userName,
                List<String> records,
                String profileImageUUID,
                String userAuthUUID) {
        this.userName = userName;
        this.records = records;
        this.profileImageUUID = profileImageUUID;
        this.userAuthUUID = userAuthUUID;
    }
}