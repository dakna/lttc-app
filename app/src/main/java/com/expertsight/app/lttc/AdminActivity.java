package com.expertsight.app.lttc;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
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
import android.widget.TextView;
import android.widget.Toast;

import com.expertsight.app.lttc.model.Transaction;
import com.expertsight.app.lttc.util.FirebaseHelper;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.expertsight.app.lttc.model.Member;

import com.expertsight.app.lttc.ui.BottomNavigationViewHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AdminActivity extends AppCompatActivity implements AddTransactionDialogFragment.AddTransactionDialogListener, EditMemberDialogFragment.EditMemberDialogListener{

    private static final String TAG = "AdminActivity";
    private static final int ACTIVITY_NUM = 2;
    private Context context = AdminActivity.this;


    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirestoreRecyclerAdapter dbAdapterAllMembers, dbAdapterAllTransactions;

    @BindView(R.id.actvMembers)
    AutoCompleteTextView autoCompleteTextView;

    @BindView(R.id.rvMembers)
    RecyclerView rvMembers;

    @BindView(R.id.rvTransactions)
    RecyclerView rvTransactions;

    @BindView(R.id.tvTotalBalance)
    TextView tvTotalBalance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        getSupportActionBar().setTitle("Administration");

        db = FirebaseFirestore.getInstance();

        ButterKnife.bind(this);
        //setupBottomNavigationView();
        setupAutoCompleteView();
        setupMemberListView();
        setupTransactionListView();
        setTotalBalance();

    }


    private void setupAutoCompleteView() {

        final ArrayAdapter<Member> adapter = new ArrayAdapter<Member>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Member member = (Member) parent.getAdapter().getItem(position);
                //Toast.makeText(context, "You clicked on " + member.getFullName() +" " + member.getId(), Toast.LENGTH_SHORT).show();
                showEditMemberDialog(member);
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

    private void setupMemberListView() {

        final CollectionReference membersRef = db.collection("/members");
        final Query query = membersRef.orderBy("firstName");

        Log.d(TAG, "starting to get Member list");

        FirestoreRecyclerOptions<Member> response = new FirestoreRecyclerOptions.Builder<Member>()
                .setQuery(query, Member.class)
                .build();


        dbAdapterAllMembers = new FirestoreRecyclerAdapter<Member, AdminActivity.MemberViewHolder>(response) {


            @Override
            public Member getItem(int position) {
                Member member = super.getItem(position);
                // fill id into local POJO so we can pass it on when clicked
                member.setId(this.getSnapshots().getSnapshot(position).getId());
                return member;
            }

            @Override
            protected void onBindViewHolder(AdminActivity.MemberViewHolder holder, int position, final Member member) {
                //Log.d(TAG, "onBindViewHolder: Member " + member.toStringIncludingAllProperties());
                holder.fullName.setText(member.getFullName());
                float balance = member.getBalance();
                holder.balance.setText("$" + String.valueOf(balance));
                holder.email.setText(member.getEmail());

                if (balance > 0) {
                    holder.balance.setTextColor(ContextCompat.getColor(context, R.color.darkGreen));
                }
                if (balance < 0) {
                    holder.balance.setTextColor(ContextCompat.getColor(context, R.color.darkRed));
                }

            }

            @Override
            public AdminActivity.MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_member_list, parent, false);

                return new AdminActivity.MemberViewHolder(view);
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

    private void setupTransactionListView() {

        final CollectionReference membersRef = db.collection("/transactions/");
        final Query query = membersRef
                .orderBy("timestamp");

        Log.d(TAG, "starting to get Transaction list" );


        FirestoreRecyclerOptions<Transaction> response = new FirestoreRecyclerOptions.Builder<Transaction>()
                .setQuery(query, Transaction.class)
                .build();


        dbAdapterAllTransactions = new FirestoreRecyclerAdapter<Transaction, AdminActivity.TransactionViewHolder>(response) {


            @Override
            public Transaction getItem(int position) {
                Transaction transaction = super.getItem(position);
                // fill id into local POJO so we can pass it on when clicked
                transaction.setId(this.getSnapshots().getSnapshot(position).getId());
                return transaction;
            }

            @Override
            protected void onBindViewHolder(AdminActivity.TransactionViewHolder holder, int position, final Transaction transaction) {
                Log.d(TAG, "onBindViewHolder: Transaction ID " + transaction.getId() + " amount " + transaction.getAmount());
                holder.subject.setText(transaction.getSubject());

                double amount = transaction.getAmount();
                holder.amount.setText("$" + amount);
                if (amount > 0) {
                    holder.amount.setTextColor(ContextCompat.getColor(context, R.color.darkGreen));
                }
                if (amount < 0) {
                    holder.amount.setTextColor(ContextCompat.getColor(context, R.color.darkRed));
                }

                // TODO: 5/22/2018 use string resource
                holder.time.setText("Paid on " + new SimpleDateFormat("MM/dd 'at' HH:mm").format(transaction.getTimestamp()));

            }

            @Override
            public AdminActivity.TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_transaction_list, parent, false);

                return new AdminActivity.TransactionViewHolder (view);
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

        dbAdapterAllTransactions.notifyDataSetChanged();
        rvTransactions.setAdapter(dbAdapterAllTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(context));
        rvTransactions.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
    }





    @Override
    public void onStart() {
        super.onStart();
        dbAdapterAllMembers.startListening();
        dbAdapterAllTransactions.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        dbAdapterAllMembers.stopListening();
        dbAdapterAllTransactions.stopListening();
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
            Member member = (Member) dbAdapterAllMembers.getItem(adapterPos);
            //Toast.makeText(context, "clicked on " + member.getId(), Toast.LENGTH_SHORT).show();
            showEditMemberDialog(member);
        }

        @Override
        public boolean onLongClick(View v) {
            int adapterPos = getAdapterPosition();
            Member member = (Member) dbAdapterAllMembers.getItem(adapterPos);
            Toast.makeText(context, "long clicked on " + member.getId(), Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder {
        public TextView subject;
        public TextView amount;
        public TextView time;

        public TransactionViewHolder(View view) {
            super(view);
            subject = view.findViewById(R.id.tvSubject);
            amount = view.findViewById(R.id.tvAmount);
            time = view.findViewById(R.id.tvTime);
        }
    }

    private void setTotalBalance() {
        Log.d(TAG, "setTotalBalance: start");
        CollectionReference members = db.collection("transactions");

        members.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed for transactions.", e);
                    return;
                }
                double totalBalance = 0;
                for (QueryDocumentSnapshot transactionDoc: queryDocumentSnapshots) {
                    Transaction transaction = transactionDoc.toObject(Transaction.class).withId(transactionDoc.getId());
                    totalBalance = totalBalance + transaction.getAmount();
                }
                tvTotalBalance.setText("$" + totalBalance);
                if (totalBalance > 0) {
                    tvTotalBalance.setTextColor(ContextCompat.getColor(context, R.color.darkGreen));
                }
                if (totalBalance < 0) {
                    tvTotalBalance.setTextColor(ContextCompat.getColor(context, R.color.darkRed));
                }
            }
        });
    }

    @OnClick(R.id.btnImportCSV)
    public void onClickImportCSV() {
        Log.d(TAG, "onClickImportCSV: start");
        FirebaseHelper fbHelper = new FirebaseHelper(context);
        fbHelper.importMemberDataFromCSV(true);
    }


    @OnClick(R.id.btnAddTransaction)
    public void onClickAddTransaction() {
        showAddTransactionDialog();
    }

    public void showAddTransactionDialog() {

        Bundle args = new Bundle();

        FragmentManager manager = getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_add_transaction_dialog");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        AddTransactionDialogFragment addTransactionDialogFragment = new AddTransactionDialogFragment();
        addTransactionDialogFragment.setArguments(args);
        addTransactionDialogFragment.show(manager, "fragment_add_transaction_dialog");

    }

    @Override
    public void applyNewTransactionData(final String subject, final double amount) {
        Log.d(TAG, "applyNewTransactionData: " + subject + " " + amount);
        Transaction newTransaction = new Transaction();
        newTransaction.setSubject(subject);
        newTransaction.setAmount(amount);
        newTransaction.setTimestamp(new Date());

        // TODO: 6/2/2018 add member ref to transaction, member id needs to be passed along with intent to start activity and stored in member

        CollectionReference transactions = db.collection("transactions");
        transactions.add(newTransaction).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful()) {
                    DocumentReference transactionRef = task.getResult();
                    Log.d(TAG, "onComplete: new transaction added with ID " + transactionRef.getId());
                    Toast.makeText(context, "Added new transaction: " + subject + " " + amount, Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onComplete: error adding new transaction");
                    Toast.makeText(context, "Unknown Error: Couldn't add new transaction", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showEditMemberDialog(Member member) {
        Log.d(TAG, "showEditMemberDialog: start " + member.getFullName());

        Bundle args = new Bundle();

        args.putString("member_id", member.getId());
        args.putString("member_firstname", member.getFirstName());
        args.putString("member_lastname", member.getLastName());
        // TODO: 6/2/2018 change float to double when refactoring
        args.putFloat("member_balance", member.getBalance());
        args.putString("member_email", member.getEmail());
        args.putBoolean("member_mailinglist", member.getIsMailingSubscriber());
        args.putString("member_smartcard_id", member.getSmartcardId());

        Date lastCheckIn = member.getLastCheckIn();
        if (lastCheckIn != null) {
            String dateString = new SimpleDateFormat("MM/dd/yyyy hh:mm").format(lastCheckIn);
            args.putString("member_last_check_in", dateString);
        }

        args.putBoolean("member_is_admin", member.getIsAdmin());
        args.putBoolean("member_is_active", member.getIsActive());


        FragmentManager manager = getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_edit_member_dialog");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        EditMemberDialogFragment editMemberDialogFragment = new EditMemberDialogFragment();
        editMemberDialogFragment.setArguments(args);
        editMemberDialogFragment.show(manager, "fragment_edit_member_dialog");

    }

    @Override
    public void applyEditMemberData(final String memberId, final String firstName, final String lastName, final String email, final boolean mailingList, final String smartcardId, final boolean isAdmin, final String lastCheckIn, final boolean isActive) {
        Log.d(TAG, "applyEditMemberData: " + memberId + " " + firstName + " " + lastName + " " + email + " " + mailingList + " " + smartcardId + " " + isAdmin + " " + lastCheckIn + " " + isActive);

        final DocumentReference memberRef = db.collection("members").document(memberId);
        memberRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "onComplete:  DocumentSnapshot data: " + document.getData());
                        final Member member = document.toObject(Member.class).withId(document.getId());

                        member.setFirstName(firstName);
                        member.setLastName(lastName);
                        member.setEmail(email);
                        member.setIsMailingSubscriber(mailingList);
                        member.setSmartcardId(smartcardId);
                        member.setIsAdmin(isAdmin);
                        try {
                            //Log.d(TAG, "onComplete: last CheckIn " + lastCheckIn);
                            Date lastCheckInDate = new SimpleDateFormat("MM/dd/yyyy hh:mm").parse(lastCheckIn);
                            //Log.d(TAG, "onComplete: last CheckIn Date " + lastCheckInDate);
                            member.setLastCheckIn(lastCheckInDate);
                            //Log.d(TAG, "onComplete: member getLastCheckIn " + member.getLastCheckIn());
                        } catch (ParseException e) {
                            Log.e(TAG, "Error parsing date string  " + lastCheckIn, e);
                            Toast.makeText(context, "Last Check-In Date not updated because " + lastCheckIn + " couldn't be parsed into Date", Toast.LENGTH_SHORT).show();
                        }
                        member.setIsActive(isActive);

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
}
