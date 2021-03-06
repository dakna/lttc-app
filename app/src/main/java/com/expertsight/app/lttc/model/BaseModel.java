package com.expertsight.app.lttc.model;


import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;


@IgnoreExtraProperties
public class BaseModel {


    private String id;
    private boolean hasPendingWrites;

    public <T extends BaseModel> T withId(@NonNull final String id) {
        this.id = id;
        return (T) this;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public boolean hasPendingWrites() {
        return hasPendingWrites;
    }

    @Exclude
    public void setHasPendingWrites(boolean hasPendingWrites) {
        this.hasPendingWrites = hasPendingWrites;
    }
}