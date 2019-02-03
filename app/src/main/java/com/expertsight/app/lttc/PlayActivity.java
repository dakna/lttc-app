package com.expertsight.app.lttc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.expertsight.app.lttc.model.Match;
import com.expertsight.app.lttc.model.Member;
import com.expertsight.app.lttc.model.Transaction;
import com.expertsight.app.lttc.ui.BottomNavigationViewHelper;
import com.expertsight.app.lttc.util.DateHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.Date;

public class PlayActivity extends AppCompatActivity
        implements CheckInFragment.OnFragmentInteractionListener, CurrentWeekFragment.OnFragmentInteractionListener, MatchFragment.OnFragmentInteractionListener,
        CheckInMemberDialogFragment.CheckInMemberDialogListener, AddMemberDialogFragment.AddMemberDialogListener, AddMatchScoreDialogFragment.AddMatchScoreDialogListener {

    // Position of fragment in view pager
    public static final int FRAGMENT_CURRENT_WEEK = 1;
    public static final int FRAGMENT_DEFAULT= 0;
    public static final String FRAGMENT_SELECT = "selected_fragment";

    private static final String TAG = "PlayActivity";
    private static final int ACTIVITY_NUM = 1;
    private Context context = PlayActivity.this;


    private FirebaseDatabase db;
    private FirebaseAnalytics analytics;

    private SectionsPagerAdapter sectionsPagerAdapter;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        setupBottomNavigationView();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Date date = new Date();
        toolbar.setTitle(getString(R.string.title_weekof, String.valueOf(DateHelper.getWeekNumber(date)), String.valueOf(DateHelper.getYear(date))));
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        db = FirebaseDatabase.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);

        if (savedInstanceState == null) {
            Intent i = getIntent();
            int defaultPosition;
            defaultPosition = i.getIntExtra(FRAGMENT_SELECT, FRAGMENT_DEFAULT);
            viewPager.setCurrentItem(defaultPosition);
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d(TAG, "onFragmentInteraction: ");
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_placeholder, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            // todo: remove unneeded params on fragments
            if (position == 0) {
                return CheckInFragment.newInstance("test", "test2");
            } else if(position ==1){
                return CurrentWeekFragment.newInstance("test", "test2");
            } else if(position ==2){
                return MatchFragment.newInstance("test", "test2");
            } else {
                return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
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

        db.getReference("members")
                .push()
                .setValue(newMember)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, getString(R.string.msg_added_new_member, firstName, lastName), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "onComplete: error adding new member");
                            Toast.makeText(context, getString(R.string.msg_error_add_member), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    public void applyCheckInData(final String memberId, final double payment, boolean keepChange) {
        Log.d(TAG, "applyCheckInMemberData: memberId: " + memberId + " payment" + payment + " keepChange " +keepChange);

        //load latest data, even if its in local cache
        final DatabaseReference memberRef = db.getReference("members").child(memberId);

        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final Member member = dataSnapshot.getValue(Member.class);
                    member.setId(dataSnapshot.getKey());
                    double newBalance = member.getBalance() - HomeActivity.FEE_PER_DAY + payment;
                    Log.d(TAG, "onComplete: calculating new member balance as $" + newBalance);
                    member.setBalance(newBalance);

                    //no server timestamp so it works offline
                    member.setLastCheckIn(new Date().getTime());
                    memberRef.setValue(member)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");

                                    Bundle params = new Bundle();
                                    params.putString("member_id", member.getId());
                                    params.putString("member_full_name", member.getFullName());
                                    params.putLong("timestamp", member.getLastCheckIn());
                                    params.putDouble("payment", payment);
                                    analytics.logEvent("club_check_in", params);

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
                        newTransaction.setMemberId(member.getId());
                        newTransaction.setSubject(getString(R.string.transaction_subject_member_fee));
                        //no server timestamp so it works offline
                        newTransaction.setTimestamp(new Date().getTime());

                        DatabaseReference transactions = db.getReference("transactions");
                        transactions.push()
                                .setValue(newTransaction)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {

                                            Log.d(TAG, "onComplete: new transaction added ");
                                            Toast.makeText(context, getString(R.string.msg_added_new_checkin_transaction,String.valueOf(payment) , member.getFullName()), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.d(TAG, "onComplete: error adding new transaction");
                                            Toast.makeText(context, getString(R.string.msg_error_new_transaction), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                } else {
                    Log.d(TAG, "No such document");
                    Toast.makeText(context, getString(R.string.msg_error_checking_in_member, memberId), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "get failed with ", databaseError.toException());
                Toast.makeText(context, getString(R.string.msg_error_checking_in_member, memberId), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void applyMatchScoreData(final String matchId, final int player1Games, final int player2Games) {
        Log.d(TAG, "applyMatchData: matchrId: " + matchId + " player1Games " + player1Games + " player2Games " + player2Games);

        //load latest data, even if its in local cache
        final DatabaseReference memberRef = db.getReference("matches").child(matchId);

        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final Match match = dataSnapshot.getValue(Match.class);
                    match.setId(dataSnapshot.getKey());

                    match.setPlayer1Games(player1Games);
                    match.setPlayer2Games(player2Games);
                    memberRef.setValue(match)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                    Bundle params = new Bundle();
                                    params.putString("match_id", match.getId());
                                    params.putInt("player_1_games", player1Games);
                                    params.putInt("player_2_games", player2Games);
                                    analytics.logEvent("match_score", params);
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
                    Toast.makeText(context, getString(R.string.msg_error_add_match_score, matchId), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "get failed with ", databaseError.toException());
                Toast.makeText(context, getString(R.string.msg_error_add_match_score, matchId), Toast.LENGTH_SHORT).show();

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

