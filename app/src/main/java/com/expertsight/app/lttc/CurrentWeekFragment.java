package com.expertsight.app.lttc;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;


import com.expertsight.app.lttc.model.Member;
import com.expertsight.app.lttc.util.FirebaseHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.google.firebase.storage.FirebaseStorage;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CurrentWeekFragment extends Fragment {

    private static final String TAG = "ActiveMemberListFragmen";

    private FirebaseDatabase db;
    private FirebaseStorage storage;
    private FirebaseRecyclerAdapter dbAdapterMembersCheckedIn;

    @BindView(R.id.rvMembers)
    RecyclerView rvMembersCheckedIn;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public CurrentWeekFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static CurrentWeekFragment newInstance(String param1, String param2) {
        CurrentWeekFragment fragment = new CurrentWeekFragment();
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
        View view = inflater.inflate(R.layout.fragment_current_week, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupMemberCheckedInListView();
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
        dbAdapterMembersCheckedIn.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        dbAdapterMembersCheckedIn.stopListening();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    private void setupMemberCheckedInListView() {

        Date startOfThisWeek = FirebaseHelper.getStartOfWeek(new Date());

        //final CollectionReference membersRef = db.collection("/members/");
        final Query query = db.getReference("/members")
                .orderByChild("lastCheckIn")
                .startAt(startOfThisWeek.getTime());
        //.whereGreaterThanOrEqualTo("lastCheckIn", startOfThisWeek)
        //.orderBy("lastCheckIn")
        //.orderBy("firstName");

        Log.d(TAG, "starting to get Member list checked in for " + new Date().getTime() + "in week starting at " + startOfThisWeek.getTime());


        FirebaseRecyclerOptions<Member> response = new FirebaseRecyclerOptions.Builder<Member>()
                .setQuery(query, Member.class)
                .build();


        dbAdapterMembersCheckedIn = new FirebaseRecyclerAdapter<Member, CurrentWeekFragment.MemberCheckedInViewHolder>(response) {


            @Override
            public Member getItem(int position) {
                Member member = super.getItem(position);
                // fill id into local POJO so we can pass it on when clicked
                member.setId(this.getSnapshots().getSnapshot(position).getKey());
                return member;
            }

            @Override
            protected void onBindViewHolder(CurrentWeekFragment.MemberCheckedInViewHolder holder, int position, final Member member) {
                Log.d(TAG, "onBindViewHolder: Member CheckedIn ID " + member.getId() + " lastCheckIn " + member.getLastCheckIn());
                holder.fullName.setText(member.getFullName());
                // TODO: 5/22/2018 use string resource
                holder.time.setText("Checked in " + new SimpleDateFormat("MM/dd 'at' hh:mm a").format(new Date(member.getLastCheckIn())));

            }

            @Override
            public CurrentWeekFragment.MemberCheckedInViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_member_checked_in_list, parent, false);

                return new CurrentWeekFragment.MemberCheckedInViewHolder(view);
            }

            @Override
            public void onError(@NonNull DatabaseError error) {
                Log.d(TAG, "onError: ");
                super.onError(error);
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
        rvMembersCheckedIn.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMembersCheckedIn.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
