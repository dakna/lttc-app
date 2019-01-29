package com.expertsight.app.lttc;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import com.expertsight.app.lttc.util.FirebaseHelper;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MatchFragment extends Fragment {

    private static final String TAG = "MatchFragment";

    private FirebaseDatabase db;
    private FirebaseStorage storage;
    private FirebaseRecyclerAdapter dbAdapterMatches;

    @BindView(R.id.rvMatches)
    RecyclerView rvMatches;


    // Start is left, End is right
    @BindView(R.id.tvPlayer1)
    TextView tvPlayer1;

    Member player1;

    @BindView(R.id.tvPlayer2)
    TextView tvPlayer2;

    Member player2;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MatchFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MatchFragment newInstance(String param1, String param2) {
        MatchFragment fragment = new MatchFragment();
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
        View view = inflater.inflate(R.layout.fragment_match_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupMatchListView();
        //setupMatchView();


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
        dbAdapterMatches.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        dbAdapterMatches.stopListening();
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
    }

    private void setupMatchListView() {

        Date startOfThisWeek = FirebaseHelper.getStartOfWeek(new Date());

        final Query query = db.getReference("/matches")
                .orderByChild("timestamp")
                .startAt(startOfThisWeek.getTime());

        Log.d(TAG, "starting to get match list played for " + new Date().getTime() + "in week starting at " + startOfThisWeek.getTime());


        FirebaseRecyclerOptions<Match> response = new FirebaseRecyclerOptions.Builder<Match>()
                .setQuery(query, Match.class)
                .build();


        dbAdapterMatches= new FirebaseRecyclerAdapter<Match, MatchFragment.MatchViewHolder>(response) {


            @Override
            public Match getItem(int position) {
                Match match = super.getItem(position);
                // fill id into local POJO so we can pass it on when clicked
                match.setId(this.getSnapshots().getSnapshot(position).getKey());
                return match;
            }

            @Override
            protected void onBindViewHolder(MatchFragment.MatchViewHolder holder, int position, final Match match) {
                Log.d(TAG, "onBindViewHolder: Match ID " + match.getId() + " timestamp " + match.getTimestamp());
                holder.player1.setText(match.getPlayer1FullName());
                holder.player1Games.setText(String.valueOf(match.getPlayer1Games()));
                if (match.getPlayer1Games() == 3) {
                    holder.player1Games.setTextColor(getResources().getColor(R.color.darkGreen));
                }
                holder.player2.setText(match.getPlayer2FullName());
                holder.player2Games.setText(String.valueOf(match.getPlayer2Games()));
                if (match.getPlayer2Games() == 3) {
                    holder.player2Games.setTextColor(getResources().getColor(R.color.darkGreen));
                }
            }

            @Override
            public MatchFragment.MatchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_match_list, parent, false);

                return new MatchFragment.MatchViewHolder(view);
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

        dbAdapterMatches.notifyDataSetChanged();
        rvMatches.setAdapter(dbAdapterMatches);
        rvMatches.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMatches.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
    }



    public class MatchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView player1;
        public TextView player2;
        public TextView player1Games;
        public TextView player2Games;

        public MatchViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            player1 = view.findViewById(R.id.tvPlayer1FullName);
            player2 = view.findViewById(R.id.tvPlayer2FullName);
            player1Games = view.findViewById(R.id.tvPlayer1Games);
            player2Games = view.findViewById(R.id.tvPlayer2Games);
        }

        @Override
        public void onClick(View v) {
            int adapterPos = getAdapterPosition();
            Match match = (Match) dbAdapterMatches.getItem(adapterPos);
            Toast.makeText(getContext(), "clicked on " + match.getId(), Toast.LENGTH_SHORT).show();
            showMatchScoreDialog(match);
        }
    }

    public void showMatchScoreDialog(Match match) {
        Log.d(TAG, "showMatchScoreDialog: Match");

        Bundle args = new Bundle();
        args.putString("match_id", match.getId());
        args.putString("player1_fullname", match.getPlayer1FullName());
        args.putString("player1_fullname", match.getPlayer2FullName());

        Log.d(TAG, "showMatchScoreDialog: player1_fullname " + match.getPlayer1FullName());

        args.putInt("player1_games", match.getPlayer1Games());
        args.putInt("player2_games", match.getPlayer2Games());

        FragmentManager manager = getActivity().getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_add_match_score_dialog");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        AddMatchScoreDialogFragment addMatchScoreDialogFragment = new AddMatchScoreDialogFragment();
        addMatchScoreDialogFragment.setArguments(args);
        addMatchScoreDialogFragment.show(manager, "fragment_add_match_score_dialog");
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
