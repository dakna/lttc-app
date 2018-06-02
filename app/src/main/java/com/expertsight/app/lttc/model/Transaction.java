package com.expertsight.app.lttc.model;


import com.google.firebase.firestore.DocumentReference;

import java.util.Date;

public class Transaction extends FirestoreModel{

    private double amount;
    private DocumentReference memberRef;
    private String subject;
    private Date timestamp;

    public Transaction() {}

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public DocumentReference getMemberRef() {
        return memberRef;
    }

    public void setMemberRef(DocumentReference memberRef) {
        this.memberRef = memberRef;
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
