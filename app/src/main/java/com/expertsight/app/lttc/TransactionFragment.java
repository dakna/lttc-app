package com.expertsight.app.lttc;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;

import com.expertsight.app.lttc.model.Transaction;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;



public class TransactionFragment extends Fragment {

    private static final String TAG = "TransactionFragment";

    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter dbAdapterAllTransactions;

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
        db = FirebaseFirestore.getInstance();
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

        final Query query = db.collection("/transactions")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        Log.d(TAG, "starting to get Transaction list" );

        FirestoreRecyclerOptions<Transaction> response = new FirestoreRecyclerOptions.Builder<Transaction>()
                .setQuery(query,  MetadataChanges.INCLUDE, Transaction.class)
                .build();

        dbAdapterAllTransactions = new FirestoreRecyclerAdapter<Transaction, TransactionFragment.TransactionViewHolder>(response) {

            @Override
            public Transaction getItem(int position) {
                Transaction transaction = super.getItem(position);
                DocumentSnapshot snapshot = this.getSnapshots().getSnapshot(position);
                transaction.setId(snapshot.getId());
                transaction.setHasPendingWrites(snapshot.getMetadata().hasPendingWrites());
                return transaction;
            }

            @Override
            protected void onBindViewHolder(TransactionFragment.TransactionViewHolder holder, int position, final Transaction transaction) {
                Log.d(TAG, "onBindViewHolder: Transaction ID " + transaction.getId() + " amount " + transaction.getAmount());

                if (transaction.hasPendingWrites()) holder.view.setBackgroundColor(getResources().getColor(R.color.colorBackgroundPendingWrite, getActivity().getTheme()));

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
                holder.time.setText(getString(R.string.transaction_paid, new SimpleDateFormat("MM/dd 'at' HH:mm").format(transaction.getTimestamp())));

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
        public View view;

        public TransactionViewHolder(View view) {
            super(view);
            this.view = view;
            subject = view.findViewById(R.id.tvSubject);
            amount = view.findViewById(R.id.tvAmount);
            time = view.findViewById(R.id.tvTime);
        }
    }

    private void setTotalBalance() {
        Log.d(TAG, "setTotalBalance: start");
        CollectionReference transactions = db.collection("transactions");


        transactions.addSnapshotListener(new EventListener<QuerySnapshot>() {
             @Override
             public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                 if (e != null) {
                     Log.w(TAG, "Listen failed.", e);
                     return;
                 }

                 double totalBalance = 0;
                 for (QueryDocumentSnapshot transactionDocument : queryDocumentSnapshots) {
                     Transaction transaction = transactionDocument.toObject(Transaction.class).withId(transactionDocument.getId());
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
