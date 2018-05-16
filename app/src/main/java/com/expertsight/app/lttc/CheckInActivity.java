package com.expertsight.app.lttc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.expertsight.app.lttc.model.Member;
import com.expertsight.app.lttc.ui.BottomNavigationViewHelper;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Collection;


public class CheckInActivity extends AppCompatActivity {

    private static final String TAG = "CheckInActivity";
    private static final int ACTIVITY_NUM = 1;
    private Context context = CheckInActivity.this;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // test only
    private static final String[] TEST_MEMBERS = new String[] {
            "Daniel Knapp", "Dan E. Knapp", "Trey Aughenbaugh", "Jeff Stiles", "Jeff Hallenbach", "Bradley Matkze"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        setupBottomNavigationView();
        setupAutoCompleteView();
    }

    private void setupAutoCompleteView() {

        db = FirebaseFirestore.getInstance();

        final ArrayAdapter<Member> adapter = new ArrayAdapter<Member>(this, android.R.layout.simple_dropdown_item_1line);
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.actvMembers);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Member member = (Member) parent.getAdapter().getItem(position);
                Toast.makeText(context, "You clicked on " + member.getFullName(), Toast.LENGTH_SHORT).show();
                parent.clearFocus();
            }
        });


        // TODO: 5/16/2018 this should register a SnapshotListener, so the activity lookup gets updated if a member is added outside the activity.
        // as of now that is not needed, since we will add members on the member activity.
        CollectionReference members = db.collection("members");
        members.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Member member = document.toObject(Member.class).withId(document.getId());
                                adapter.add(member);
                            }
                        } else {
                            Log.d(TAG, "Error getting members: ", task.getException());
                        }
                    }
                });


    }



    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(context, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
