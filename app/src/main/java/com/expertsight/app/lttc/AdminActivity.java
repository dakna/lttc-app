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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.expertsight.app.lttc.model.Transaction;
import com.expertsight.app.lttc.ui.BottomNavigationViewHelper;
import com.expertsight.app.lttc.util.FirebaseHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.storage.FirebaseStorage;
import com.expertsight.app.lttc.model.Member;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

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


    private FirebaseDatabase db;
    private FirebaseStorage storage;
    private FirebaseRecyclerAdapter dbAdapterAllMembers, dbAdapterAllTransactions;

    private String memberId;

    @BindView(R.id.actvMembers)
    AutoCompleteTextView autoCompleteTextView;

    @BindView(R.id.rvMembers)
    RecyclerView rvMembers;

    @BindView(R.id.rvTransactions)
    RecyclerView rvTransactions;

    @BindView(R.id.tvTotalBalance)
    TextView tvTotalBalance;

    @BindView(R.id.btnImportCSV)
    Button btnImportCSV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        getSupportActionBar().setTitle("Administration");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupBottomNavigationView();

        db = FirebaseDatabase.getInstance();

        ButterKnife.bind(this);

        // all members imported for now, so we disable import button
        btnImportCSV.setEnabled(false);

        setupAutoCompleteView();
        setupMemberListView();
        setupTransactionListView();
        setTotalBalance();
        Log.d(TAG, "onCreate: Intent admin memberId " + getIntent().getStringExtra("memberId"));
        memberId = getIntent().getStringExtra("memberId");

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


        DatabaseReference members = db.getReference("members");
        members.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                adapter.clear();
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, memberSnapshot.getKey() + " => " + memberSnapshot.getValue());
                    Member member = memberSnapshot.getValue(Member.class);
                    adapter.add(member);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: no members in list");
            }
        });
    }

    private void setupMemberListView() {

        //final CollectionReference membersRef = db.collection("/members");
        final Query query = db.getReference("members")
                .orderByChild("firstName");

        Log.d(TAG, "starting to get Member list");

        FirebaseRecyclerOptions<Member> response = new FirebaseRecyclerOptions.Builder<Member>()
                .setQuery(query, Member.class)
                .build();


        dbAdapterAllMembers = new FirebaseRecyclerAdapter<Member, AdminActivity.MemberViewHolder>(response) {


            @Override
            public Member getItem(int position) {
                Member member = super.getItem(position);
                // fill id into local POJO so we can pass it on when clicked
                member.setId(this.getSnapshots().getSnapshot(position).getKey());
                return member;
            }

            @Override
            protected void onBindViewHolder(AdminActivity.MemberViewHolder holder, int position, final Member member) {
                //Log.d(TAG, "onBindViewHolder: Member " + member.toStringIncludingAllProperties());
                holder.fullName.setText(member.getFullName());
                double balance = member.getBalance();
                holder.balance.setText("$" + String.valueOf(balance));
                holder.email.setText(member.getEmail());

                if (balance > 0) {
                    holder.balance.setTextColor(ContextCompat.getColor(context, R.color.darkGreen));
                } else if (balance < 0) {
                    holder.balance.setTextColor(ContextCompat.getColor(context, R.color.darkRed));
                } else {
                    holder.balance.setTextColor(ContextCompat.getColor(context, R.color.grey));
                }
                if (member.isPlayingToday()) {
                    holder.playingToday.setVisibility(View.VISIBLE);
                } else {
                    holder.playingToday.setVisibility(View.GONE);
                }
            }

            @Override
            public AdminActivity.MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_member_list, parent, false);

                return new AdminActivity.MemberViewHolder(view);
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

        //final CollectionReference membersRef = db.collection("/transactions/");
        final Query query = db.getReference("/transactions")
                .orderByChild("timestamp");

        Log.d(TAG, "starting to get Transaction list" );


        FirebaseRecyclerOptions<Transaction> response = new FirebaseRecyclerOptions.Builder<Transaction>()
                .setQuery(query, Transaction.class)
                .build();


        dbAdapterAllTransactions = new FirebaseRecyclerAdapter<Transaction, AdminActivity.TransactionViewHolder>(response) {


            @Override
            public Transaction getItem(int position) {
                Transaction transaction = super.getItem(position);
                // fill id into local POJO so we can pass it on when clicked
                transaction.setId(this.getSnapshots().getSnapshot(position).getKey());
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
                } else if (amount < 0) {
                    holder.amount.setTextColor(ContextCompat.getColor(context, R.color.darkRed));
                } else {
                    holder.amount.setTextColor(ContextCompat.getColor(context, R.color.grey));
                }

                // TODO: 5/22/2018 use string resource
                holder.time.setText("Paid on " + new SimpleDateFormat("MM/dd 'at' HH:mm").format(new Date(transaction.getTimestamp())));

            }

            @Override
            public AdminActivity.TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_transaction_list, parent, false);

                return new AdminActivity.TransactionViewHolder (view);
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
        public ImageView playingToday;

        public MemberViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            fullName = view.findViewById(R.id.tvFullName);
            email = view.findViewById(R.id.tvEmail);
            balance = view.findViewById(R.id.tvMemberBalance);
            playingToday = view.findViewById(R.id.ivPlayingToday);
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
        DatabaseReference members = db.getReference("transactions");

        members.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalBalance = 0;
                for (DataSnapshot transactionSnapshot: dataSnapshot.getChildren()) {
                    Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                    transaction.setId(transactionSnapshot.getKey());
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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        newTransaction.setTimestamp(new Date().getTime());
        DatabaseReference memberRef = db.getReference("members").child(memberId);
        newTransaction.setMemberRef(memberRef.toString());

        // TODO: 6/2/2018 add member ref to transaction, member id needs to be passed along with intent to start activity and stored in member

        DatabaseReference transactions = db.getReference("transactions");
        transactions.push()
                .setValue(newTransaction)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "onComplete: new transaction added");
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
        args.putDouble("member_balance", member.getBalance());
        args.putString("member_email", member.getEmail());
        args.putBoolean("member_mailinglist", member.getIsMailingSubscriber());
        args.putString("member_smartcard_id", member.getSmartcardId());


        if (member.getLastCheckIn() != 0) {
            Date lastCheckIn = new Date(member.getLastCheckIn());
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
    public void applyEditMemberData(final String memberId, final String firstName, final String lastName, final String email, final boolean mailingList, final String smartcardId, final boolean isAdmin, final String lastCheckIn, final boolean isActive, final String balance) {
        Log.d(TAG, "applyEditMemberData: " + memberId + " " + firstName + " " + lastName + " " + email + " " + mailingList + " " + smartcardId + " " + isAdmin + " " + lastCheckIn + " " + isActive);

        final DatabaseReference memberRef = db.getReference("members").child(memberId);
        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        Log.d(TAG, "onComplete:  DocumentSnapshot data: " + dataSnapshot.getValue());
                        final Member member = dataSnapshot.getValue(Member.class);
                        member.setId(dataSnapshot.getKey());

                        member.setFirstName(firstName);
                        member.setLastName(lastName);
                        member.setEmail(email);
                        member.setIsMailingSubscriber(mailingList);
                        member.setSmartcardId(smartcardId);
                        member.setIsAdmin(isAdmin);
                        if (lastCheckIn.length() > 0) {
                            try {
                                Date lastCheckInDate = new SimpleDateFormat("MM/dd/yyyy hh:mm").parse(lastCheckIn);
                                member.setLastCheckIn(lastCheckInDate.getTime());
                            } catch (ParseException e) {
                                Log.e(TAG, "Error parsing date string  " + lastCheckIn, e);
                                Toast.makeText(context, "Last Check-In Date not updated because " + lastCheckIn + " couldn't be parsed into Date", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            member.setLastCheckIn(0);
                        }

                        member.setIsActive(isActive);

                        if (balance.length() > 0) {
                            try {
                                double balanceDouble = Double.valueOf(balance);
                                member.setBalance(balanceDouble);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing balance string to float" + balance, e);
                                Toast.makeText(context, "Balance not updated because " + balance + " couldn't be parsed into Double", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            member.setBalance(0d);
                        }


                        memberRef.setValue(member)
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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "get failed with ", databaseError.toException());
                    Toast.makeText(context, "Error while checking in member " + memberId, Toast.LENGTH_SHORT).show();
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
