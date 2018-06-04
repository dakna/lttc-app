package com.expertsight.app.lttc.model;

import android.util.Log;

import com.expertsight.app.lttc.util.FirebaseHelper;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Calendar;
import java.util.Date;

public class Member extends FirestoreModel {

    private static final String TAG = "Member";

    private String firstName;
    private String lastName;
    private String email;
    private String smartcardId;
    private boolean isMailingSubscriber;
    private boolean isActive;
    private boolean isAdmin;
    private boolean hasSignedWaiver;

    // TODO: 6/2/2018 refactor float to double
    private float balance;
    private Date lastCheckIn;


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

    public boolean getIsMailingSubscriber() {
        return isMailingSubscriber;
    }

    public void setIsMailingSubscriber(boolean mailingSubscriber) {
        isMailingSubscriber = mailingSubscriber;
    }

    public boolean getIsActive() {
        return isActive;
    }

    // firestore POJO mapper needs the exact fieldname as getter and ignores the convention for boolean fields
    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean admin) {
        isAdmin = admin;
    }

    public void setIsActive(boolean active) {
        isActive = active;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public Date getLastCheckIn() { return lastCheckIn; }

    public void setLastCheckIn(Date lastCheckIn) {
        this.lastCheckIn = lastCheckIn;
    }

    public boolean getHasSignedWaiver() {
        return hasSignedWaiver;
    }

    public void setHasSignedWaiver(boolean hasSignedWaiver) {
        this.hasSignedWaiver = hasSignedWaiver;
    }

    @Exclude
    public boolean isPlayingToday() {
        Date lastCheckIn = getLastCheckIn();
        if (lastCheckIn == null) return false;

        Date startOfToday = FirebaseHelper.getStartOfDay(new Date());

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(startOfToday);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(lastCheckIn);

        if (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
            Log.d(TAG, "isPlayingToday: true");
            return true;
        }

        
        return false;
    }

    @Exclude
    public boolean isPlayingThisWeek() {
        Date lastCheckIn = getLastCheckIn();
        if (lastCheckIn == null) return false;

        Date startOfWeek= FirebaseHelper.getStartOfWeek(new Date());

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(startOfWeek);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(lastCheckIn);

        if (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
            Log.d(TAG, "isPlayingThisWeek: true");
            return true;
        }


        return false;
    }

    @Exclude
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return getFullName();
    }

    public String toStringIncludingAllProperties() {
        return "Member{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", smartcardId='" + smartcardId + '\'' +
                ", isMailingSubscriber=" + isMailingSubscriber +
                ", isActive=" + isActive +
                ", isAdmin=" + isAdmin +
                ", balance=" + balance +
                ", lastCheckIn=" + lastCheckIn +
                ", hasSignedWaiver=" + hasSignedWaiver +
                '}';
    }
}
