package com.expertsight.app.lttc.model;



import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Transaction extends FirestoreModel{

    private double amount;
    private String memberId;
    private String subject;
    private long timestamp;

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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
