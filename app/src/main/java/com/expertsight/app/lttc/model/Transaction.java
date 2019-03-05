package com.expertsight.app.lttc.model;



import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class Transaction extends BaseModel {

    private double amount;
    private String memberId;
    private String subject;
    private Date timestamp;
    private boolean hasPendingWrites;

    public Transaction() {}

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberRef) {
        this.memberId = memberId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public boolean hasPendingWrites() {
        return hasPendingWrites;
    }

    @Exclude
    public void setHasPendingWrites(boolean hasPendingWrites) {
        this.hasPendingWrites = hasPendingWrites;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "amount=" + amount +
                ", memberRef=" + memberId +
                ", subject='" + subject + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
