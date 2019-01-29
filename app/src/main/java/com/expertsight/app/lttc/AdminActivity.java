package com.expertsight.app.lttc;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.expertsight.app.lttc.model.Member;
import com.expertsight.app.lttc.ui.BottomNavigationViewHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminActivity extends AppCompatActivity implements MemberFragment.OnFragmentInteractionListener,
        EditMemberDialogFragment.EditMemberDialogListener {

    private static final String TAG = "AdminActivity";
    private static final int ACTIVITY_NUM = 2;
    private Context context = AdminActivity.this;


    private FirebaseDatabase db;

    private SectionsPagerAdapter sectionsPagerAdapter;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        setupBottomNavigationView();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
    }


/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play, menu);
        return true;
    }*/

/*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d(TAG, "onFragmentInteraction: ");
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
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
                return MemberFragment.newInstance("test", "test2");
            } else {
                return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

/*    @Override
    public void applyNewTransactionData(final String subject, final double amount) {
        Log.d(TAG, "applyNewTransactionData: " + subject + " " + amount);
        Transaction newTransaction = new Transaction();
        newTransaction.setSubject(subject);
        newTransaction.setAmount(amount);
        newTransaction.setTimestamp(new Date().getTime());
        DatabaseReference memberRef = db.getReference("members").child(memberId);
        newTransaction.setMemberId(memberRef.toString());

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
    }*/

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

