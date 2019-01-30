package com.expertsight.app.lttc;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;


import com.expertsight.app.lttc.model.Match;
import com.expertsight.app.lttc.model.Member;
import com.expertsight.app.lttc.model.Transaction;
import com.expertsight.app.lttc.util.FirebaseHelper;
import com.expertsight.app.lttc.widget.MemberListWidgetService;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import com.google.firebase.storage.FirebaseStorage;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CurrentWeekFragment extends Fragment {

    private static final String TAG = "CurrentWeekFragment";

    private FirebaseDatabase db;
    private FirebaseStorage storage;
    private FirebaseRecyclerAdapter dbAdapterMembersCheckedIn;

    @BindView(R.id.rvMembers)
    RecyclerView rvMembersCheckedIn;


    // Start is left, End is right
    @BindView(R.id.tvPlayer1)
    TextView tvPlayer1;

    Member player1;

    @BindView(R.id.tvPlayer2)
    TextView tvPlayer2;

    Member player2;
    @BindView(R.id.fabMatch)
    FloatingActionButton fabMatch;

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
        setupMatchView();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                Log.d(TAG, "onSwiped: " + viewHolder.getAdapterPosition());
                player1 = (Member) dbAdapterMembersCheckedIn.getItem(viewHolder.getAdapterPosition());
                setupMatchView();
                rvMembersCheckedIn.getAdapter().notifyDataSetChanged();

            }
        }).attachToRecyclerView(rvMembersCheckedIn);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                Log.d(TAG, "onSwiped: " + viewHolder.getAdapterPosition());
                player2 = (Member) dbAdapterMembersCheckedIn.getItem(viewHolder.getAdapterPosition());
                setupMatchView();
                rvMembersCheckedIn.getAdapter().notifyDataSetChanged();

            }
        }).attachToRecyclerView(rvMembersCheckedIn);

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


    private void setupMatchView() {
        if (player1 != null) {
            tvPlayer1.setText(player1.getFullName());
        } else {
            tvPlayer1.setText("Add Player 1");
        }

        if (player2 != null) {
            tvPlayer2.setText(player2.getFullName());
        } else {
            tvPlayer2.setText("Add Player 2");
        }

        if ((player1 != null) && (player2 != null)) {
            fabMatch.setVisibility(View.VISIBLE);

        } else {
            fabMatch.setVisibility(View.INVISIBLE);
        }


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
                updateWidget(this);
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

    
    private void updateWidget(FirebaseRecyclerAdapter adapter) {
        List<Member> memberList = new ArrayList<>();

        Iterator iterator = adapter.getSnapshots().listIterator();
        while (iterator.hasNext()) {
            memberList.add((Member) iterator.next());
        }
        MemberListWidgetService.updateWidget(getActivity(), memberList);
    }
    
    
    @OnClick(R.id.fabMatch)
    public void onClickFabMatch() {
        if ((player1 != null) && (player2 != null)) {
            addMatch(player1, player2);
            player1 = null;
            player2 = null;
            setupMatchView();
        }
    }

    public void addMatch(final Member player1, final Member player2) {
        Log.d(TAG, "addMatch: ");

        Match match = new Match();
        match.setPlayer1Id(player1.getId());
        match.setPlayer1FullName(player1.getFullName());

        match.setPlayer2Id(player2.getId());
        match.setPlayer2FullName(player2.getFullName());
        //no server timestamp so it works offline
        match.setTimestamp(new Date().getTime());

        DatabaseReference matches = db.getReference("matches");
        matches.push()
                .setValue(match)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {

                            Log.d(TAG, "onComplete: new match added ");
                            Toast.makeText(getContext(), "Added match " + player1.getFullName() + " : " + player2.getFullName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "onComplete: error adding new match");
                            Toast.makeText(getContext(), "Unknown Error: Couldn't add new match", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
