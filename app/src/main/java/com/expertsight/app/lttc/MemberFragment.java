package com.expertsight.app.lttc;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.expertsight.app.lttc.model.Member;
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
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MemberFragment extends Fragment {

    private static final String TAG = "MemberFragment";

    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter dbAdapterAllMembers;

    @BindView(R.id.actvMembers)
    AutoCompleteTextView autoCompleteTextView;


    @BindView(R.id.rvMembers)
    RecyclerView rvMembers;

    @BindView(R.id.btnAddMember)
    Button btnAddMember;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MemberFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MemberFragment newInstance(String param1, String param2) {
        MemberFragment fragment = new MemberFragment();
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
        View view = inflater.inflate(R.layout.fragment_member, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupAutoCompleteView();
        setupMemberListView();
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
        dbAdapterAllMembers.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        dbAdapterAllMembers.stopListening();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setupAutoCompleteView() {
        final ArrayAdapter<Member> adapter = new ArrayAdapter<Member>(getContext(), android.R.layout.simple_dropdown_item_1line);
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Member member = (Member) parent.getAdapter().getItem(position);
                if (((AdminActivity) getActivity()).initializedByAdmin()) {
                    showEditMemberDialog(member);
                } else {
                    Toast.makeText(getContext(), getString(R.string.msg_only_for_admin), Toast.LENGTH_SHORT).show();
                }
                autoCompleteTextView.setText("");
                autoCompleteTextView.clearFocus();
            }
        });

        autoCompleteTextView.setOnDismissListener(new AutoCompleteTextView.OnDismissListener() {
            @Override
            public void onDismiss() {
                InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(getActivity().getCurrentFocus().getApplicationWindowToken(), 0);
            }
        });


        CollectionReference members = db.collection("members");
        Query query = members.orderBy("firstName");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                if (queryDocumentSnapshots != null) {
                    adapter.clear();
                    for (QueryDocumentSnapshot memberSnapshot : queryDocumentSnapshots) {
                        Log.d(TAG, memberSnapshot.getId() + " => " + memberSnapshot.getData());
                        Member member = memberSnapshot.toObject(Member.class).withId(memberSnapshot.getId());
                        adapter.add(member);
                    }
                }
            }
        });
    }

    private void setupMemberListView() {

        final Query query = db.collection("members")
                .orderBy("firstName");

        Log.d(TAG, "starting to get Member list");

        FirestoreRecyclerOptions<Member> response = new FirestoreRecyclerOptions.Builder<Member>()
                .setQuery(query, MetadataChanges.INCLUDE, Member.class)
                .build();


        dbAdapterAllMembers = new FirestoreRecyclerAdapter<Member, MemberFragment.MemberViewHolder>(response) {


            @Override
            public Member getItem(int position) {
                Member member = super.getItem(position);
                DocumentSnapshot snapshot = this.getSnapshots().getSnapshot(position);
                member.setId(snapshot.getId());
                member.setHasPendingWrites(snapshot.getMetadata().hasPendingWrites());
                return member;
            }

            @Override
            protected void onBindViewHolder(MemberFragment.MemberViewHolder holder, int position, final Member member) {
                //Log.d(TAG, "onBindViewHolder: Member " + member.toStringIncludingAllProperties());

                if (member.hasPendingWrites()) holder.view.setBackgroundColor(getResources().getColor(R.color.colorBackgroundPendingWrite, getActivity().getTheme()));

                holder.fullName.setText(member.getFullName());
                double balance = member.getBalance();
                holder.balance.setText(getString(R.string.dollar_amount, String.valueOf(balance)));
                holder.email.setText(member.getEmail());

                if (balance > 0) {
                    holder.balance.setTextColor(ContextCompat.getColor(getContext(), R.color.darkGreen));
                } else if (balance < 0) {
                    holder.balance.setTextColor(ContextCompat.getColor(getContext(), R.color.darkRed));
                } else {
                    holder.balance.setTextColor(ContextCompat.getColor(getContext(), R.color.grey));
                }
                if (member.isPlayingThisWeek()) {
                    holder.playingToday.setVisibility(View.VISIBLE);
                } else {
                    holder.playingToday.setVisibility(View.GONE);
                }
            }

            @Override
            public MemberFragment.MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_member_list, parent, false);

                return new MemberFragment.MemberViewHolder(view);
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
        rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMembers.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
    }


    public class MemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView fullName;
        public TextView email;
        public TextView balance;
        public ImageView playingToday;
        public View view;

        public MemberViewHolder(View view) {
            super(view);
            this.view = view;
            view.setOnClickListener(this);
            fullName = view.findViewById(R.id.tvFullName);
            email = view.findViewById(R.id.tvEmail);
            balance = view.findViewById(R.id.tvMemberBalance);
            playingToday = view.findViewById(R.id.ivPlayingToday);
        }

        @Override
        public void onClick(View v) {
            int adapterPos = getAdapterPosition();
            Member member = (Member) dbAdapterAllMembers.getItem(adapterPos);

            if (((AdminActivity) getActivity()).initializedByAdmin()) {
                showEditMemberDialog(member);
            } else {
                Toast.makeText(getContext(), getString(R.string.msg_only_for_admin), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @OnClick(R.id.btnAddMember)
    public void onClickAddMember() {

        if (((AdminActivity) getActivity()).initializedByAdmin()) {
            showAddMemberDialog(null);
        } else {
            Toast.makeText(getContext(), getString(R.string.msg_only_for_admin), Toast.LENGTH_SHORT).show();
        }
    }


    public void showAddMemberDialog(String hexId) {
        Log.d(TAG, "showAddMemberDialog: start " + hexId);

        Bundle args = new Bundle();
        if (hexId != null) {
            args.putString("smartcard_id", hexId);
        }

        FragmentManager manager = getActivity().getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_add_member_dialog");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        AddMemberDialogFragment addMemberDialogFragment = new AddMemberDialogFragment();
        addMemberDialogFragment.setArguments(args);
        addMemberDialogFragment.show(manager, "fragment_add_member_dialog");

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


        if (member.getLastCheckIn() != null) {
            String dateString = new SimpleDateFormat("MM/dd/yyyy hh:mm").format(member.getLastCheckIn());
            args.putString("member_last_check_in", dateString);
        }

        args.putBoolean("member_is_admin", member.getIsAdmin());
        args.putBoolean("member_is_active", member.getIsActive());


        FragmentManager manager = getActivity().getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_edit_member_dialog");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        EditMemberDialogFragment editMemberDialogFragment = new EditMemberDialogFragment();
        editMemberDialogFragment.setArguments(args);
        editMemberDialogFragment.show(manager, "fragment_edit_member_dialog");

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
