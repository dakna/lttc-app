package com.expertsight.app.lttc;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.expertsight.app.lttc.model.Member;

import com.expertsight.app.lttc.ui.BottomNavigationViewHelper;

public class MemberActivity extends AppCompatActivity {

    private static final String TAG = "MemberActivity";
    private static final int ACTIVITY_NUM = 2;
    private Context context = MemberActivity.this;


    private FirestoreRecyclerAdapter dbAdapter;
    private FirebaseFirestore db;
    private RecyclerView rvPairs;
    private LinearLayoutManager layoutManager;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: 5/10/2018 concept check: member list open for everyone or behind login and only for admin?
/*        FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();
        if (authUser == null) {
            startActivity(MainActivity.createIntent(this));
            finish();
            return;
        }*/

        Log.d(TAG, "onCreate: before layout");
        setContentView(R.layout.activity_member);
        setupBottomNavigationView();
        getSupportActionBar().setTitle("Pairs");


        layoutManager = new LinearLayoutManager(this);
        rvPairs = findViewById(R.id.rvMembers);
        //rvPairs.setHasFixedSize(true);


        db = FirebaseFirestore.getInstance();
        dbAdapter = setupPairListAdapter();
        rvPairs.setAdapter(dbAdapter);
        rvPairs.setLayoutManager(layoutManager);



    }


    private FirestoreRecyclerAdapter setupPairListAdapter() {

        final CollectionReference usersRef = db.collection("/members/");
        Query query = usersRef.orderBy("firstName");

        Log.d(TAG, "starting to get Member list");

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "size of the snapshots after get and result " + String.valueOf(task.getResult().size()));
                        }
                    }
                });



        FirestoreRecyclerOptions<Member> response = new FirestoreRecyclerOptions.Builder<Member>()
                .setQuery(query, Member.class)
                .build();


        FirestoreRecyclerAdapter mAdapter = new FirestoreRecyclerAdapter<Member, UserViewHolder>(response) {


            @Override
            public Member getItem(int position) {
                Member member = super.getItem(position);
                // fill id into local POJO so we can pass it on when clicked
                member.setId(this.getSnapshots().getSnapshot(position).getId());
                return member;
            }

            @Override
            protected void onBindViewHolder(UserViewHolder holder, int position, final Member member) {
                Log.d(TAG, "onBindViewHolder: Member ID " + member.getId());
                holder.tvFullName.setText(member.getId() + " " + member.getLastName());

            }

            @Override
            public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_member_list, parent, false);

                return new UserViewHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("onError : error ", e.getMessage());
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                Log.d(TAG,"on Data changed");
            }

            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);
            }

        };

        mAdapter.notifyDataSetChanged();
        return mAdapter;
    }


    @Override
    public void onStart() {
        super.onStart();
        dbAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        dbAdapter.stopListening();
    }

    public class UserViewHolder extends ViewHolder {
        public TextView tvFullName;

        public UserViewHolder(View view) {
            super(view);
            tvFullName = view.findViewById(R.id.tvFullName);
        }
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
