package com.expertsight.app.lttc.model;


import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;


@IgnoreExtraProperties
public class BaseModel {


    @Exclude
    private String id;

    public <T extends BaseModel> T withId(@NonNull final String id) {
        this.id = id;
        return (T) this;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}