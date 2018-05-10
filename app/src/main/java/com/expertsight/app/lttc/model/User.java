package com.expertsight.app.lttc.model;


import com.google.firebase.firestore.DocumentReference;

public class User extends FirestoreModel{

    private String username;
    private DocumentReference defaultPairUser;
    private String profilePictureFilename;

    // TODO: 4/10/2018 add profile pic

    // for Firestore
    public User() {}


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public DocumentReference getDefaultPairUser() {
        return defaultPairUser;
    }

    public void setDefaultPairUser(DocumentReference defaultPairUser) {
        this.defaultPairUser = defaultPairUser;
    }

    public String getProfilePictureFilename() {
        return profilePictureFilename;
    }

    public void setProfilePictureFilename(String profilePictureFilename) {
        this.profilePictureFilename = profilePictureFilename;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", defaultPairUser=" + defaultPairUser +
                ", profilePictureFilename='" + profilePictureFilename + '\'' +
                '}';
    }
}
