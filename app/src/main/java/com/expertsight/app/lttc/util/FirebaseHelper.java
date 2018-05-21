package com.expertsight.app.lttc.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";

    //firebase
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private FirebaseFirestore db;

    private StorageReference storageRef;
    private String userID;


    //vars
    private Context context;
    private double mPhotoUploadProgress = 0;

    public FirebaseHelper(Context context) {
        auth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();


        storageRef = FirebaseStorage.getInstance().getReference();
        this.context = context;

        if(auth.getCurrentUser() != null){
            userID = auth.getCurrentUser().getUid();
        }
    }



    public boolean hasCurrentUser() {
        if (auth.getCurrentUser() != null) {
            return true;
        }
        return false;
    }

    public void processUserLogin() {
        Log.d(TAG, "processUserLogin: starting");
        FirebaseUser fbUser = auth.getCurrentUser();

        // This checks if a user object has to be initialized in firestore.
        // todo We could set a local flag, so we don't try to read  every login, only update the lastLogin
        final DocumentReference docRef = db.collection("users").document(fbUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Log.d(TAG, "processUserLogin: Existing user found in user collection with data: " + document.getData());
                        Map<String, Object> existingUserData = new HashMap<>();
                        // this is an existing user , so only update lastLogin.
                        existingUserData.put("lastLogin", FieldValue.serverTimestamp());

                        docRef.update(existingUserData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: Existing User updated");
                                    }

                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "onFailure: Existing User could not be updated",e);
                                        Toast.makeText(context, "Existing User could not be updated", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.d(TAG, " processUserLogin: New user not found in user collection with id " + document.getId());

                        // @todo should use a method to generate all necessary data structures, likes etc.
                        Map<String, Object> newUserData = new HashMap<>();
                        // this is a new user, so firstLogin == lastLogin.
                        newUserData.put("firstLogin", FieldValue.serverTimestamp());
                        newUserData.put("lastLogin", FieldValue.serverTimestamp());

                        docRef.set(newUserData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: New User initialized");
                                    Toast.makeText(context, "New User initialized", Toast.LENGTH_SHORT).show();

                                }

                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "onFailure: New User could not be initialized",e);
                                    Toast.makeText(context, "New User could not be initialized", Toast.LENGTH_SHORT).show();
                                }
                            });


                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    // for working with Timestamp fields in Collection queries
    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

}












































