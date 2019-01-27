package com.expertsight.app.lttc.model;



import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Transaction extends FirestoreModel{

    private double amount;
    private String memberRef;
    private String subject;
    private long timestamp;

    public Transaction() {}

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMemberRef() {
        return memberRef;
    }

    public void setMemberRef(String memberRef) {
        this.memberRef = memberRef;
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
                ", memberRef=" + memberRef +
                ", subject='" + subject + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
