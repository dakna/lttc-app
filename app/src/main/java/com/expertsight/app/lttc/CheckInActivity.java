package com.expertsight.app.lttc;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.expertsight.app.lttc.model.Member;
import com.expertsight.app.lttc.util.FirebaseHelper;
import com.expertsight.app.lttc.util.MifareHelper;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CheckInActivity extends AppCompatActivity implements AddMemberDialogFragment.AddMemberDialogListener, CheckInMemberDialogFragment.CheckInMemberDialogListener{

    private static final String TAG = "CheckInActivity";
    private static final int ACTIVITY_NUM = 1;
    private Context context = CheckInActivity.this;

    public static final String MIME_TEXT_PLAIN = "text/plain";


    private NfcAdapter mNfcAdapter = null;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirestoreRecyclerAdapter dbAdapterAllMembers, dbAdapterMembersCheckedIn;

    @BindView(R.id.actvMembers)
    AutoCompleteTextView autoCompleteTextView;

    @BindView(R.id.btnAddMember)
    Button btnAddMember;

    @BindView(R.id.rvMembers)
    RecyclerView rvMembers;

    @BindView(R.id.rvMembersCheckedIn)
    RecyclerView rvMembersCheckedIn;

    @BindView(R.id.tvNFC)
    TextView mTextView;

    @BindView(R.id.tvMemberFullName)
    TextView tvMemberFullName;

    @BindView(R.id.tvMemberBalance)
    TextView tvMemberBalance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        setContentView(R.layout.activity_check_in);





        ButterKnife.bind(this);
        //setupBottomNavigationView();
        setupAutoCompleteView();
        setupMemberListView();
        setupMemberCheckedInListView();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;

        }

        if (!mNfcAdapter.isEnabled()) {
            mTextView.setText("NFC is disabled.");
        } else {
            mTextView.setText("NFC is enabled for MifareClassic");
        }

        handleIntent(getIntent());
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
    public void showAddMemberDialog() {
        Log.d(TAG, "showAddMemberDialog: start");
        FragmentManager manager = getFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_add_member_dialog");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        AddMemberDialogFragment addMemberDialogFragment = new AddMemberDialogFragment();
        addMemberDialogFragment.show(manager, "fragment_add_member_dialog");

    }


    public void showCheckInMemberDialog(Member member) {
        Log.d(TAG, "showCheckInMemberDialog: Member");

        if (member.isPlayingToday()) {
            Toast.makeText(context, "This member already checked in today.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle args = new Bundle();
        args.putString("member_id", member.getId());
        args.putString("member_fullname", member.getFullName());
        args.putFloat("member_balance", member.getBalance());

        FragmentManager manager = getFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_check_in_member_dialog");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        CheckInMemberDialogFragment checkInMemberDialogFragment = new CheckInMemberDialogFragment();
        checkInMemberDialogFragment.setArguments(args);
        checkInMemberDialogFragment.show(manager, "fragment_check_in_member_dialog");

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
        dbAdapterMembersCheckedIn.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        dbAdapterAllMembers.stopListening();
        dbAdapterMembersCheckedIn.stopListening();
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView fullName;
        public TextView email;

        public MemberViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            fullName = view.findViewById(R.id.tvFullName);
            email = view.findViewById(R.id.tvEmail);
        }

        @Override
        public void onClick(View v) {
            int adapterPos = getAdapterPosition();
            Member member = (Member) dbAdapterAllMembers.getItem(adapterPos);
            Toast.makeText(context, "clicked on " + member.getId(), Toast.LENGTH_SHORT).show();
            showCheckInMemberDialog(member);
        }
    }

    public class MemberCheckedInViewHolder extends RecyclerView.ViewHolder {
        public TextView fullName;

        public MemberCheckedInViewHolder(View view) {
            super(view);
            fullName = view.findViewById(R.id.tvFullName);
        }
    }

    private void setupMemberCheckedInListView() {

        Date startOfToday = FirebaseHelper.getStartOfDay(new Date());

        final CollectionReference membersRef = db.collection("/members/");
        final Query query = membersRef
                            .whereGreaterThanOrEqualTo("lastCheckIn", startOfToday)
                            .orderBy("lastCheckIn")
                            .orderBy("firstName");

        Log.d(TAG, "starting to get Member list checked in for " + new Date());


        FirestoreRecyclerOptions<Member> response = new FirestoreRecyclerOptions.Builder<Member>()
                .setQuery(query, Member.class)
                .build();


        dbAdapterMembersCheckedIn = new FirestoreRecyclerAdapter<Member, MemberCheckedInViewHolder>(response) {


            @Override
            public Member getItem(int position) {
                Member member = super.getItem(position);
                // fill id into local POJO so we can pass it on when clicked
                member.setId(this.getSnapshots().getSnapshot(position).getId());
                return member;
            }

            @Override
            protected void onBindViewHolder(MemberCheckedInViewHolder holder, int position, final Member member) {
                Log.d(TAG, "onBindViewHolder: Member CheckedIn ID " + member.getId() + " lastCheckIn " + member.getLastCheckIn());
                holder.fullName.setText(member.getFullName());

            }

            @Override
            public MemberCheckedInViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_member_checked_in_list, parent, false);

                return new MemberCheckedInViewHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("onError : error ", e.getMessage());
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                Log.d(TAG,"on Data changed for members checked in today");
            }

            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);
            }

        };

        dbAdapterMembersCheckedIn.notifyDataSetChanged();
        rvMembersCheckedIn.setAdapter(dbAdapterMembersCheckedIn);
        rvMembersCheckedIn.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false));
        rvMembersCheckedIn.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL));
    }


    @Override
    protected void onResume() {
        super.onResume();

        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {

        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }


    @Override
    protected void onNewIntent(Intent intent) {

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.d(TAG, "handleIntent: entry");

        String action = intent.getAction();
        mTextView.setText("handling intent with action" + action);

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {



            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.d(TAG, "handleIntent: tag" + tag);

            byte[] id = tag.getId();

            String hexId = MifareHelper.getHexString(id, id.length);
            Log.d(TAG, "handleIntent: tag ID in HEX " + hexId);



            //member lookup
            CollectionReference membersRef = db.collection("/members/");
            final Query query = membersRef.whereEqualTo("smartcardId", hexId);

            query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots.size() == 1) {
                        DocumentSnapshot memberDoc = queryDocumentSnapshots.getDocuments().get(0);
                        Member member = memberDoc.toObject(Member.class).withId(memberDoc.getId());
                        //setupMemberCheckInView(member);
                        Log.d(TAG, "onSuccess getting member by smartcard lookup: " + member.toString());
                        mTextView.setText("Found a member with Smartcard ID " + member.getSmartcardId());
                        tvMemberFullName.setText(member.getFullName());
                        tvMemberBalance.setText("Balance: $" + member.getBalance());
                    }
                }
            });

        }
    }


    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }


    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }


/*    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(context, this, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }*/

    @Override
    public void applyNewMemberData(final String firstName, final String lastName, String email, boolean mailingList) {
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

    @Override
    public void applyCheckInData(String memberId, float payment, boolean keepChange) {
        Log.d(TAG, "applyCheckInMemberData: memberId: " + memberId + " payment" + payment + " keepChange " +keepChange);
/*        Member newMember = new Member();
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
        });*/
    }
}
