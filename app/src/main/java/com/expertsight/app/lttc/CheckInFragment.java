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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.expertsight.app.lttc.model.Member;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CheckInFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CheckInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CheckInFragment extends Fragment {

    private static final String TAG = "ActiveMemberListFragmen";

    private FirebaseDatabase db;
    private FirebaseStorage storage;
    private FirebaseRecyclerAdapter dbAdapterActiveMembers;

    @BindView(R.id.actvMembers)
    AutoCompleteTextView autoCompleteTextView;

    @BindView(R.id.btnAddMember)
    Button btnAddMember;

    @BindView(R.id.rvMembers)
    RecyclerView rvMembers;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public CheckInFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CheckInFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CheckInFragment newInstance(String param1, String param2) {
        CheckInFragment fragment = new CheckInFragment();
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
        View view = inflater.inflate(R.layout.fragment_check_in, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupAutoCompleteView();
        setupMemberActiveListView();
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
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        dbAdapterActiveMembers.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        dbAdapterActiveMembers.stopListening();
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
                //Toast.makeText(context, "You clicked on " + member.getFullName() +" " + member.getId(), Toast.LENGTH_SHORT).show();
                showCheckInMemberDialog(member);
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

        FragmentManager manager = getActivity().getSupportFragmentManager();
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
            Toast.makeText(getContext(), "This member already checked in this week", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle args = new Bundle();
        args.putString("member_id", member.getId());
        args.putString("member_fullname", member.getFullName());
        args.putDouble("member_balance", member.getBalance());

        FragmentManager manager = getActivity().getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_check_in_member_dialog");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        CheckInMemberDialogFragment checkInMemberDialogFragment = new CheckInMemberDialogFragment();
        checkInMemberDialogFragment.setArguments(args);
        checkInMemberDialogFragment.show(manager, "fragment_check_in_member_dialog");
    }



    private void setupMemberActiveListView() {

        //final CollectionReference membersRef = db.collection("/members");
        final Query query = db.getReference("members")
                //.equalTo("isActive", "true")
                .orderByChild("firstName");

        Log.d(TAG, "starting to get Member list");

        FirebaseRecyclerOptions<Member> response = new FirebaseRecyclerOptions.Builder<Member>()
                .setQuery(query, Member.class)
                .build();

        int defaultTextColor = 0;

        dbAdapterActiveMembers = new FirebaseRecyclerAdapter<Member, CheckInFragment.MemberViewHolder>(response) {


            @Override
            public Member getItem(int position) {
                Member member = super.getItem(position);
                // fill id into local POJO so we can pass it on when clicked
                member.setId(this.getSnapshots().getSnapshot(position).getKey());
                return member;
            }

            @Override
            protected void onBindViewHolder(CheckInFragment.MemberViewHolder holder, int position, Member member) {
                Log.d(TAG, "onBindViewHolder: Member ID " + member.getId());
                holder.fullName.setText(member.getFullName());
                Log.d(TAG, "onBindViewHolder: fullname textsize" + holder.fullName.getTextSize());
                BigDecimal balance = new BigDecimal(member.getBalance());
                Log.d(TAG, "onBindViewHolder: balance " + balance);
                holder.balance.setText("$" + String.valueOf(balance));


                holder.email.setText(member.getEmail());
                Log.d(TAG, "onBindViewHolder: email textsize" + holder.email.getTextSize());

                // BigDecimal test
                int compareZeroPlus = balance.compareTo(new BigDecimal("0.0"));
                int compareZeroMinus = balance.compareTo(new BigDecimal("-0.0"));
                if (compareZeroPlus > 0) {
                    holder.balance.setTextColor(ContextCompat.getColor(getContext(), R.color.darkGreen));
                } else if (compareZeroMinus < 0) {
                    holder.balance.setTextColor(ContextCompat.getColor(getContext(), R.color.darkRed));
                } else {
                    holder.balance.setTextColor(ContextCompat.getColor(getContext(), R.color.grey));
                }
                if (member.isPlayingToday()) {
                    holder.playingToday.setVisibility(View.VISIBLE);
                } else {
                    holder.playingToday.setVisibility(View.GONE);
                }

            }

            @Override
            public CheckInFragment.MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_member_list, parent, false);

                return new CheckInFragment.MemberViewHolder(view);
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
        rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMembers.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView fullName;
        public TextView email;
        public TextView balance;
        public ImageView playingToday;

        public MemberViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            fullName = view.findViewById(R.id.tvFullName);
            email = view.findViewById(R.id.tvEmail);
            balance = view.findViewById(R.id.tvMemberBalance);
            playingToday = view.findViewById(R.id.ivPlayingToday);
        }

        @Override
        public void onClick(View v) {
            int adapterPos = getAdapterPosition();
            Member member = (Member) dbAdapterActiveMembers.getItem(adapterPos);
            //Toast.makeText(context, "clicked on " + member.getId(), Toast.LENGTH_SHORT).show();
            showCheckInMemberDialog(member);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
