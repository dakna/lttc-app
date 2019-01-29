package com.expertsight.app.lttc.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.expertsight.app.lttc.R;
import com.expertsight.app.lttc.model.Member;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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

    public static Date getStartOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Log.d(TAG, "getStartOfWeek: " + calendar.getTime());
        return calendar.getTime();
    }

        // todo: stil using firestore
    public void importMemberDataFromCSV(boolean skipFirstLine) {
        InputStream is = context.getResources().openRawResource(R.raw.contactsbadge);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8")));
        String line = "";

        ArrayList memberList = new ArrayList();

        try {
            if (skipFirstLine) reader.readLine();
            while ((line = reader.readLine()) != null) {
                // Split the line into different tokens (using the comma as a separator).
                String[] tokens = line.split(",");
                //Log.d(TAG, "importMemberDataFromCSV: tokens size " + tokens.length);

                // Read the data and store it in the WellData POJO.
                Member member = new Member();
                member.setFirstName(tokens[0]);
                member.setLastName(tokens[1]);
                member.setEmail(tokens[2]);

                // HexIDs are enclosed in string tags
                String smartcardId = tokens[3];
                member.setSmartcardId(smartcardId.replace("\"", ""));

                boolean waiverSigned = false;
                // after split token[4] is only set if there is a string in it
                if ((tokens.length >= 5) && (tokens[4].equals("TRUE"))) {
                    waiverSigned = true;
                }
                member.setHasSignedWaiver(waiverSigned);

                // members in CSV are considered active
                member.setIsActive(true);

                if (tokens[2].length() > 0) {
                    member.setIsMailingSubscriber(true);
                }

                if ((tokens[2].equals("opendroid@gmail.com")) || (tokens[2].equals("treyaughenbaugh@gmail.com"))) {
                    member.setIsAdmin(true);
                }

                //Log.d(TAG ,"importMemberDataFromCSV: Just added to list " + member.toStringIncludingAllProperties());
                memberList.add(member);
            }
        } catch (IOException e1) {
            Log.e(TAG, "Error" + line, e1);
            e1.printStackTrace();
        }

        Log.d(TAG, "importMemberDataFromCSV: memberList size " + memberList.size());

        Iterator iter = memberList.iterator();

        while (iter.hasNext()) {
            // test collection for now
            CollectionReference members = db.collection("/members");
            final Member member = (Member) iter.next();
            members.add(member).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if(task.isSuccessful()) {
                        DocumentReference memberRef = task.getResult();
                        Log.d(TAG, "onComplete: new member " + member.toString() + " added with ID " + memberRef.getId());
                    } else {
                        Log.d(TAG, "onComplete: error adding new member " + member.toString());
                        Toast.makeText(context, "Unknown Error: Couldn't add new member " + member.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }



    }
}












































