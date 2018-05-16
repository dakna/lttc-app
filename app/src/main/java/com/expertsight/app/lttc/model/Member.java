package com.expertsight.app.lttc.model;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

public class Member extends FirestoreModel {

    private String firstName;
    private String lastName;
    private String email;
    private String smartcardId;
    private boolean isMailingSubscriber;
    private boolean isActive;
    private float balance;
    // maybe just authID and that is the check if you are an admin? instead of extra user object. we could keep the username in firebase auth
    //private DocumentReference userRef;


    public Member() {}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSmartcardId() {
        return smartcardId;
    }

    public void setSmartcardId(String smartcardId) {
        this.smartcardId = smartcardId;
    }

    public boolean isMailingSubscriber() {
        return isMailingSubscriber;
    }

    public void setMailingSubscriber(boolean mailingSubscriber) {
        isMailingSubscriber = mailingSubscriber;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    @Exclude
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
