package com.expertsight.app.lttc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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
import com.expertsight.app.lttc.model.Transaction;
import com.expertsight.app.lttc.util.FirebaseHelper;
import com.expertsight.app.lttc.util.MifareHelper;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CheckInActivity extends AppCompatActivity implements AddMemberDialogFragment.AddMemberDialogListener, CheckInMemberDialogFragment.CheckInMemberDialogListener, AdminBottomSheetDialogFragment.AdminBottomSheetDialogListener {

    private static final String TAG = "CheckInActivity";
    private static final int ACTIVITY_NUM = 1;
    private Context context = CheckInActivity.this;

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final double FEE_PER_DAY = 5f;


    private NfcAdapter mNfcAdapter = null;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirestoreRecyclerAdapter dbAdapterActiveMembers, dbAdapterMembersCheckedIn;

    @BindView(R.id.actvMembers)
    AutoCompleteTextView autoCompleteTextView;

    @BindView(R.id.btnAddMember)
    Button btnAddMember;

    @BindView(R.id.rvMembers)
    RecyclerView rvMembers;

    @BindView(R.id.rvMembersCheckedIn)
    RecyclerView rvMembersCheckedIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        db = FirebaseFirestore.getInstance();

        ButterKnife.bind(this);
        //setupBottomNavigationView();
        setupAutoCompleteView();
        setupMemberActiveListView();
        setupMemberCheckedInListView();

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC smart cards", Toast.LENGTH_LONG).show();
/*
            finish();
            return;
*/

        } else {
            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(this, "NFC is disabled, please enable to read smart cards", Toast.LENGTH_LONG).show();
            }
        }

        // this is Daniel Knapp's id
        //testAdminDialog("dPSiUYvkre4BfyKiHidf");
        handleIntent(getIntent());
    }

    private void setupAutoCompleteView() {

        final ArrayAdapter<Member> adapter = new ArrayAdapter<Member>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Member member = (Member) parent.getAdapter().getItem(position);
                //Toast.makeText(context, "You clicked on " + member.getFullName() +" " + member.getId(), Toast.LENGTH_SHORT).show();
                showCheckInMemberDialog(member);
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


        CollectionReference members = db.collection("members");
        members.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                adapter.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Log.d(TAG, document.getId() + " => " + document.getData());
                    Member member = document.toObject(Member.class).withId(document.getId());
                    adapter.add(member);
                }
            }
        });

    }

    @OnClick(R.id.btnAddMember)
    public void onClickAddMember() {
        showAddMemberDialog(null);
    }


    public void showAddMemberDialog(String hexId) {
        Log.d(TAG, "showAddMemberDialog: start " + hexId);

        Bundle args = new Bundle();
        if (hexId != null) {
            args.putString("smartcard_id", hexId);
        }

        FragmentManager manager = getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_add_member_dialog");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        AddMemberDialogFragment addMemberDialogFragment = new AddMemberDialogFragment();
        addMemberDialogFragment.setArguments(args);
        addMemberDialogFragment.show(manager, "fragment_add_member_dialog");

    }


    public void showCheckInMemberDialog(Member member) {
        Log.d(TAG, "showCheckInMemberDialog: Member");

        //if (member.isPlayingThisWeek()) {
        if (member.isPlayingToday()) {
            Toast.makeText(context, "This member already checked in this week", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle args = new Bundle();
        args.putString("member_id", member.getId());
        args.putString("member_fullname", member.getFullName());
        args.putDouble("member_balance", member.getBalance());

        FragmentManager manager = getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_check_in_member_dialog");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        CheckInMemberDialogFragment checkInMemberDialogFragment = new CheckInMemberDialogFragment();
        checkInMemberDialogFragment.setArguments(args);
        checkInMemberDialogFragment.show(manager, "fragment_check_in_member_dialog");
    }

    public void showAdminDialog(Member member) {
        Log.d(TAG, "showAdminDialog: start ");

        Bundle args = new Bundle();
        args.putString("member_id", member.getId());

        FragmentManager manager = getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_admin_dialog");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        AdminBottomSheetDialogFragment adminDialogFragment = new AdminBottomSheetDialogFragment();
        adminDialogFragment.setArguments(args);
        adminDialogFragment.show(manager, "fragment_admin_dialog");

    }



    private void setupMemberActiveListView() {

        final CollectionReference membersRef = db.collection("/members");
        final Query query = membersRef
                .whereEqualTo("isActive", true)
                .orderBy("firstName");

        Log.d(TAG, "starting to get Member list");

        FirestoreRecyclerOptions<Member> response = new FirestoreRecyclerOptions.Builder<Member>()
                .setQuery(query, Member.class)
                .build();

        int defaultTextColor = 0;

        dbAdapterActiveMembers = new FirestoreRecyclerAdapter<Member, MemberViewHolder>(response) {


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
                BigDecimal balance = new BigDecimal(member.getBalance());
                Log.d(TAG, "onBindViewHolder: balance " + balance);
                holder.balance.setText("$" + String.valueOf(balance));


                holder.email.setText(member.getEmail());

                // BigDecimal test
                int compareZeroPlus = balance.compareTo(new BigDecimal("0.0"));
                int compareZeroMinus = balance.compareTo(new BigDecimal("-0.0"));
                if (compareZeroPlus > 0) {
                    holder.balance.setTextColor(ContextCompat.getColor(context, R.color.darkGreen));
                } else if (compareZeroMinus < 0) {
                    holder.balance.setTextColor(ContextCompat.getColor(context, R.color.darkRed));
                } else {
                    holder.balance.setTextColor(ContextCompat.getColor(context, R.color.grey));
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

        dbAdapterActiveMembers.notifyDataSetChanged();
        rvMembers.setAdapter(dbAdapterActiveMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(context));
        rvMembers.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
    }


    @Override
    public void onStart() {
        super.onStart();
        dbAdapterActiveMembers.startListening();
        dbAdapterMembersCheckedIn.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        dbAdapterActiveMembers.stopListening();
        dbAdapterMembersCheckedIn.stopListening();
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView fullName;
        public TextView email;
        public TextView balance;

        public MemberViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            fullName = view.findViewById(R.id.tvFullName);
            email = view.findViewById(R.id.tvEmail);
            balance = view.findViewById(R.id.tvMemberBalance);
        }

        @Override
        public void onClick(View v) {
            int adapterPos = getAdapterPosition();
            Member member = (Member) dbAdapterActiveMembers.getItem(adapterPos);
            //Toast.makeText(context, "clicked on " + member.getId(), Toast.LENGTH_SHORT).show();
            showCheckInMemberDialog(member);
        }

        @Override
        public boolean onLongClick(View v) {
            int adapterPos = getAdapterPosition();
            Member member = (Member) dbAdapterActiveMembers.getItem(adapterPos);
            Toast.makeText(context, "long clicked on " + member.getId(), Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    public class MemberCheckedInViewHolder extends RecyclerView.ViewHolder {
        public TextView fullName;
        public TextView time;

        public MemberCheckedInViewHolder(View view) {
            super(view);
            fullName = view.findViewById(R.id.tvFullName);
            time = view.findViewById(R.id.tvTime);
        }
    }

    private void setupMemberCheckedInListView() {

        Date startOfThisWeek = FirebaseHelper.getStartOfWeek(new Date());

        final CollectionReference membersRef = db.collection("/members/");
        final Query query = membersRef
                            .whereGreaterThanOrEqualTo("lastCheckIn", startOfThisWeek)
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
                // TODO: 5/22/2018 use string resource
                holder.time.setText("Checked in " + new SimpleDateFormat("MM/dd 'at' hh:mm a").format(member.getLastCheckIn()));

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
        rvMembersCheckedIn.setLayoutManager(new LinearLayoutManager(context));
        rvMembersCheckedIn.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcAdapter != null) {
            setupForegroundDispatch(this, mNfcAdapter);
        }

    }

    @Override
    protected void onPause() {

        if (mNfcAdapter != null) {
            stopForegroundDispatch(this, mNfcAdapter);
        }

        super.onPause();
    }


    @Override
    protected void onNewIntent(Intent intent) {

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.d(TAG, "handleIntent: entry");

        String action = intent.getAction();

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {



            //clear itent because it will re-run on orientation change. card should be scanned only once
            intent.setAction("");

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.d(TAG, "handleIntent: tag" + tag);

            byte[] id = tag.getId();

            final String hexId = MifareHelper.getHexString(id, id.length);
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

                        Log.d(TAG, "onSuccess getting member by smartcard id " + member.getSmartcardId() + ": " + member.toString());
                        if (member.getIsAdmin() == true) {
                            Log.d(TAG, "onComplete: member is admin");
                            //toastTotalBalance();
                            showAdminDialog(member);
                        } else {
                            showCheckInMemberDialog(member);
                        }
                    } else if (queryDocumentSnapshots.size() > 1){
                        Toast.makeText(context, "Error: The smartcard ID " + hexId + " is assigned to more than one member", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Couldn't find a member with the smartcard ID " + hexId, Toast.LENGTH_LONG).show();
                        showAddMemberDialog(hexId);
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



    @Override
    public void applyNewMemberData(final String firstName, final String lastName, String email, boolean mailingList, String smartcardId) {
        Log.d(TAG, "applyMemberData: " + firstName + " " + lastName + " " + email + " " + mailingList + " " + smartcardId);
        Member newMember = new Member();
        newMember.setFirstName(firstName);
        newMember.setLastName(lastName);
        newMember.setEmail(email);
        newMember.setIsMailingSubscriber(mailingList);
        newMember.setSmartcardId(smartcardId);
        //all new members should be active
        newMember.setIsActive(true);
        newMember.setIsAdmin(false);
        newMember.setBalance(0f);

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
    public void applyCheckInData(final String memberId, final double payment, boolean keepChange) {
        Log.d(TAG, "applyCheckInMemberData: memberId: " + memberId + " payment" + payment + " keepChange " +keepChange);

        //load latest data, even if its in local cache
        final DocumentReference memberRef = db.collection("members").document(memberId);
        memberRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "onComplete:  DocumentSnapshot data: " + document.getData());
                        final Member member = document.toObject(Member.class).withId(document.getId());
                        double newBalance = member.getBalance() - CheckInActivity.FEE_PER_DAY + payment;
                        Log.d(TAG, "onComplete: calculating new member balance as $" + newBalance);
                        member.setBalance(newBalance);

                        //no server timestamp so it works offline
                        member.setLastCheckIn(new Date());
                        memberRef.set(member, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });

                        // no $0 transactions if member uses his balance, because he prepaid
                        if (payment != 0) {
                            Transaction newTransaction = new Transaction();
                            newTransaction.setAmount(payment);
                            newTransaction.setMemberRef(memberRef);
                            newTransaction.setSubject(getString(R.string.transaction_subject_member_fee));
                            //no server timestamp so it works offline
                            newTransaction.setTimestamp(new Date());

                            CollectionReference transactions = db.collection("transactions");
                            transactions.add(newTransaction).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if(task.isSuccessful()) {
                                        DocumentReference memberRef = task.getResult();
                                        Log.d(TAG, "onComplete: new transaction added with ID " + memberRef.getId());
                                        Toast.makeText(context, "Added new transaction: " + payment + " from " + member.getFullName(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.d(TAG, "onComplete: error adding new transaction");
                                        Toast.makeText(context, "Unknown Error: Couldn't add new transaction", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(context, "Error while checking in member " + memberId, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Toast.makeText(context, "Error while checking in member " + memberId, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void applyAdminDialogData(String memberId, int buttonSelection) {
        if (buttonSelection == R.id.btnCheckIn) {

            final DocumentReference memberRef = db.collection("members").document(memberId);
            memberRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                  @Override
                  public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                      if (task.isSuccessful()) {
                          DocumentSnapshot document = task.getResult();
                          if (document.exists()) {
                              Log.d(TAG, "onComplete:  DocumentSnapshot data: " + document.getData());
                              final Member member = document.toObject(Member.class).withId(document.getId());
                              showCheckInMemberDialog(member);
                          }
                      }
                  }
              });




        } else if (buttonSelection == R.id.btnAdmin) {
            Intent intent = new Intent(context, AdminActivity.class);
            intent.putExtra("memberId" , memberId);
            startActivity(intent);
        }
    }


    public void testAdminDialog(String memberId) {
        final DocumentReference memberRef = db.collection("members").document(memberId);
        memberRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "onComplete:  DocumentSnapshot data: " + document.getData());
                        final Member member = document.toObject(Member.class).withId(document.getId());
                        showAdminDialog(member);
                    }
                }
            }
        });
    }
}
