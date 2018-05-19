package com.expertsight.app.lttc;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.expertsight.app.lttc.model.Member;
import com.expertsight.app.lttc.ui.BottomNavigationViewHelper;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CheckInActivity extends AppCompatActivity implements AddMemberDialogFragment.AddMemberDialogListener{

    private static final String TAG = "CheckInActivity";
    private static final int ACTIVITY_NUM = 1;
    private Context context = CheckInActivity.this;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirestoreRecyclerAdapter dbAdapterAllMembers, dbAdapterCheckedInMembers;

    @BindView(R.id.actvMembers)
    AutoCompleteTextView autoCompleteTextView;

    @BindView(R.id.btnAddMember)
    Button btnAddMember;

    @BindView(R.id.rvMembers)
    RecyclerView rvMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        setContentView(R.layout.activity_check_in);
        ButterKnife.bind(this);
        setupBottomNavigationView();
        setupAutoCompleteView();
        setupMemberListView();
    }

    private void setupAutoCompleteView() {

        final ArrayAdapter<Member> adapter = new ArrayAdapter<Member>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Member member = (Member) parent.getAdapter().getItem(position);
                Toast.makeText(context, "You clicked on " + member.getFullName() +" " + member.getId(), Toast.LENGTH_SHORT).show();
                autoCompleteTextView.setText("");
                autoCompleteTextView.clearFocus();
            }
        });

        autoCompleteTextView.setOnDismissListener(new AutoCompleteTextView.OnDismissListener() {
            @Override
            public void onDismiss() {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), 0);
            }
        });


        // TODO: 5/16/2018 this should register a SnapshotListener, so the activity lookup gets updated if a member is added outside the activity.
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

    @OnClick(R.id.btnAddMember)
    public void addMember() {
        Log.d(TAG, "addMember: start");
        FragmentManager manager = getFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_add_member_dialog");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        AddMemberDialogFragment addMemberDialogFragment = new AddMemberDialogFragment();
        addMemberDialogFragment.show(manager, "fragment_add_member_dialog");

    }


    private void setupMemberListView() {

        final CollectionReference membersRef = db.collection("/members/");
        final Query query = membersRef.orderBy("firstName");

        Log.d(TAG, "starting to get Member list");
/*

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "size of the snapshots after get and result " + String.valueOf(task.getResult().size()));
                        }
                    }
                });
*/



        FirestoreRecyclerOptions<Member> response = new FirestoreRecyclerOptions.Builder<Member>()
                .setQuery(query, Member.class)
                .build();


        dbAdapterAllMembers = new FirestoreRecyclerAdapter<Member, MemberViewHolder>(response) {


            @Override
            public Member getItem(int position) {
                Member member = super.getItem(position);
                // fill id into local POJO so we can pass it on when clicked
                member.setId(this.getSnapshots().getSnapshot(position).getId());
                return member;
            }

            @Override
            protected void onBindViewHolder(MemberViewHolder holder, int position, final Member member) {
                Log.d(TAG, "onBindViewHolder: Member ID " + member.getId());
                holder.fullName.setText(member.getFullName());
                String email = member.getEmail();
                if (!email.isEmpty()) {
                    holder.email.setText(member.getEmail());
                }

            }

            @Override
            public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_member_list, parent, false);

                return new MemberViewHolder(view);
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

        dbAdapterAllMembers.notifyDataSetChanged();
        rvMembers.setAdapter(dbAdapterAllMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(context));
        rvMembers.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
    }


    @Override
    public void onStart() {
        super.onStart();
        dbAdapterAllMembers.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        dbAdapterAllMembers.stopListening();
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder {
        public TextView fullName;
        public TextView email;

        public MemberViewHolder(View view) {
            super(view);
            fullName = view.findViewById(R.id.tvFullName);
            email = view.findViewById(R.id.tvEmail);
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

    @Override
    public void applyNewMemberData(final String firstName, final String lastName, String email, Boolean mailingList) {
        Log.d(TAG, "applyMemberData: " + firstName + " " + lastName + " " + email +" " + mailingList);
        Member newMember = new Member();
        newMember.setFirstName(firstName);
        newMember.setLastName(lastName);
        newMember.setEmail(email);
        newMember.setMailingSubscriber(mailingList);

        CollectionReference members = db.collection("members");
        members.add(newMember).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful()) {
                    DocumentReference memberRef = task.getResult();
                    Log.d(TAG, "onComplete: new member added with ID " + memberRef.getId());
                    Toast.makeText(context, "Added new member: " + firstName + " " + lastName, Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onComplete: error adding new member");
                    Toast.makeText(context, "Unknown Error: Couldn't add new member", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
