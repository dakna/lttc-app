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
import com.expertsight.app.lttc.model.Transaction;
import com.expertsight.app.lttc.ui.BottomNavigationViewHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminActivity extends AppCompatActivity implements MemberFragment.OnFragmentInteractionListener, TransactionFragment.OnFragmentInteractionListener,
        EditMemberDialogFragment.EditMemberDialogListener, AddTransactionDialogFragment.AddTransactionDialogListener, AddMemberDialogFragment.AddMemberDialogListener {

    private static final String TAG = "AdminActivity";
    private static final int ACTIVITY_NUM = 2;
    private Context context = AdminActivity.this;

    private String adminMemberId;

    private FirebaseFirestore db;
    private FirebaseAnalytics analytics;

    private SectionsPagerAdapter sectionsPagerAdapter;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        setupBottomNavigationView();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle(getString(R.string.title_section_admin));
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        db = FirebaseFirestore.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);

        Log.d(TAG, "onCreate: Intent admin adminMemberId " + getIntent().getStringExtra("adminMemberId"));
        adminMemberId = getIntent().getStringExtra("adminMemberId");

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
                return MemberFragment.newInstance("test", "test2");
            } else if (position == 1) {
                    return TransactionFragment.newInstance("test", "test2");
            } else {
                return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public void applyEditMemberData(final String memberId, final String firstName, final String lastName, final String email, final boolean mailingList, final String smartcardId, final boolean isAdmin, final String lastCheckIn, final boolean isActive, final String balance) {
        Log.d(TAG, "applyEditMemberData: " + memberId + " " + firstName + " " + lastName + " " + email + " " + mailingList + " " + smartcardId + " " + isAdmin + " " + lastCheckIn + " " + isActive);

        final DocumentReference memberRef = db.collection("members").document(memberId);
        memberRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        Log.d(TAG, "onComplete:  DocumentSnapshot data: " + document.getId());
                        final Member member = document.toObject(Member.class).withId(document.getId());;
                        member.setFirstName(firstName);
                        member.setLastName(lastName);
                        member.setEmail(email);
                        member.setIsMailingSubscriber(mailingList);
                        member.setSmartcardId(smartcardId);
                        member.setIsAdmin(isAdmin);
                        if (lastCheckIn.length() > 0) {
                            try {
                                Date lastCheckInDate = new SimpleDateFormat("MM/dd/yyyy hh:mm").parse(lastCheckIn);
                                member.setLastCheckIn(lastCheckInDate);
                            } catch (ParseException e) {
                                Log.e(TAG, "Error parsing date string  " + lastCheckIn, e);
                                Toast.makeText(context, getString(R.string.msg_last_checkin_error_begin) + lastCheckIn + getString(R.string.msg_last_checkin_eror_end), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            member.setLastCheckIn(null);
                        }

                        member.setIsActive(isActive);

                        if (balance.length() > 0) {
                            try {
                                double balanceDouble = Double.valueOf(balance);
                                member.setBalance(balanceDouble);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "Error parsing balance string to float" + balance, e);
                                Toast.makeText(context, getString(R.string.msg_balance_error_begin) + balance + getString(R.string.msg_balance_error_end), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            member.setBalance(0d);
                        }


                        memberRef.set(member, SetOptions.merge())
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
                                })
                        ;
                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(context, getString(R.string.msg_error_checkin_member) + memberId, Toast.LENGTH_SHORT).show();
                    }
                }  else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Toast.makeText(context, getString(R.string.msg_error_checkin_member) + memberId, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void applyNewMemberData(final String firstName, final String lastName, String email, boolean mailingList, String smartcardId) {
        Log.d(TAG, "applyMemberData: " + firstName + " " + lastName + " " + email + " " + mailingList + " " + smartcardId);
        final Member newMember = new Member();
        newMember.setFirstName(firstName);
        newMember.setLastName(lastName);
        newMember.setEmail(email);
        newMember.setIsMailingSubscriber(mailingList);
        newMember.setSmartcardId(smartcardId);
        //all new members should be active
        newMember.setIsActive(true);
        newMember.setIsAdmin(false);
        newMember.setBalance(0f);

        CollectionReference members = db.collection("members");
        members.add(newMember).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: new member added with ID " + newMember.getId());
                    Toast.makeText(context, getString(R.string.msg_added_new_member, firstName, lastName), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onComplete: error adding new member");
                    Toast.makeText(context, getString(R.string.msg_error_add_member), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void applyNewTransactionData(final String subject, final double amount) {
        Log.d(TAG, "applyNewTransactionData: " + subject + " " + amount);

        final Transaction newTransaction = new Transaction();
        newTransaction.setSubject(subject);
        newTransaction.setAmount(amount);
        newTransaction.setTimestamp(new Date());
        if (initializedByAdmin()) {
            DocumentReference memberRef = db.collection("members").document(adminMemberId);
            newTransaction.setMemberId(memberRef.toString());
        }

        CollectionReference transactions = db.collection("transactions");
        DocumentReference ref = transactions.document();
        Log.d(TAG, "applyNewTransactionData: test doc ref path = " + ref.getPath());

        transactions.add(newTransaction)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "onComplete: new transaction added with ID " + newTransaction.getId());
                    Toast.makeText(context, getString(R.string.msg_added_new_transaction) + subject + " " + amount, Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onComplete: error adding new transaction");
                    Toast.makeText(context, getString(R.string.msg_error_new_transaction), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean initializedByAdmin() {
        if ((adminMemberId != null) && (!adminMemberId.isEmpty())) return true;
        return false;
    }

    public void showPendingSync(Boolean isSyncPending) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (isSyncPending) toolbar.setSubtitle("Sync Pending");
        else toolbar.setSubtitle("");
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

