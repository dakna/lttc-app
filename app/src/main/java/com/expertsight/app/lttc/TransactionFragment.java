package com.expertsight.app.lttc;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.expertsight.app.lttc.model.Member;
import com.expertsight.app.lttc.model.Transaction;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;


public class TransactionFragment extends Fragment {

    private static final String TAG = "TransactionFragment";

    private FirebaseDatabase db;
    private FirebaseRecyclerAdapter dbAdapterAllTransactions;

    @BindView(R.id.rvTransactions)
    RecyclerView rvTransactions;

    @BindView(R.id.tvTotalBalance)
    TextView tvTotalBalance;




    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public TransactionFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TransactionFragment newInstance(String param1, String param2) {
        TransactionFragment fragment = new TransactionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        db = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupTransactionListView();
        setTotalBalance();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + getString(R.string.implement_OnFragmentInteractionListener));
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        dbAdapterAllTransactions.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        dbAdapterAllTransactions.stopListening();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setupTransactionListView() {

        final Query query = db.getReference("/transactions")
                .orderByChild("timestamp");

        Log.d(TAG, "starting to get Transaction list" );


        FirebaseRecyclerOptions<Transaction> response = new FirebaseRecyclerOptions.Builder<Transaction>()
                .setQuery(query, Transaction.class)
                .build();


        dbAdapterAllTransactions = new FirebaseRecyclerAdapter<Transaction, TransactionFragment.TransactionViewHolder>(response) {


            @Override
            public Transaction getItem(int position) {
                Transaction transaction = super.getItem(position);
                // fill id into local POJO so we can pass it on when clicked
                transaction.setId(this.getSnapshots().getSnapshot(position).getKey());
                return transaction;
            }

            @Override
            protected void onBindViewHolder(TransactionFragment.TransactionViewHolder holder, int position, final Transaction transaction) {
                Log.d(TAG, "onBindViewHolder: Transaction ID " + transaction.getId() + " amount " + transaction.getAmount());
                holder.subject.setText(transaction.getSubject());

                double amount = transaction.getAmount();
                holder.amount.setText(getString(R.string.dollar_amount, String.valueOf(amount)));
                if (amount > 0) {
                    holder.amount.setTextColor(ContextCompat.getColor(getContext(), R.color.darkGreen));
                } else if (amount < 0) {
                    holder.amount.setTextColor(ContextCompat.getColor(getContext(), R.color.darkRed));
                } else {
                    holder.amount.setTextColor(ContextCompat.getColor(getContext(), R.color.grey));
                }

                // TODO: 5/22/2018 use string resource
                holder.time.setText(getString(R.string.transaction_paid, new SimpleDateFormat("MM/dd 'at' HH:mm").format(new Date(transaction.getTimestamp()))));

            }

            @Override
            public TransactionFragment.TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_transaction_list, parent, false);

                return new TransactionFragment.TransactionViewHolder (view);
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
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
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
                tvTotalBalance.setText(getString(R.string.dollar_amount, String.valueOf(totalBalance)));
                if (totalBalance > 0) {
                    tvTotalBalance.setTextColor(ContextCompat.getColor(getContext(), R.color.darkGreen));
                }
                if (totalBalance < 0) {
                    tvTotalBalance.setTextColor(ContextCompat.getColor(getContext(), R.color.darkRed));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @OnClick(R.id.btnAddTransaction)
    public void onClickAddTransaction() {
        if (((AdminActivity) getActivity()).initializedByAdmin()) {
            showAddTransactionDialog();
        } else {
            Toast.makeText(getContext(), getString(R.string.msg_only_for_admin), Toast.LENGTH_SHORT).show();
        }

    }

    public void showAddTransactionDialog() {

        Bundle args = new Bundle();

        FragmentManager manager = getActivity().getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_add_transaction_dialog");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        AddTransactionDialogFragment addTransactionDialogFragment = new AddTransactionDialogFragment();
        addTransactionDialogFragment.setArguments(args);
        addTransactionDialogFragment.show(manager, "fragment_add_transaction_dialog");

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
