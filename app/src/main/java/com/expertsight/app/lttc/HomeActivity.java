package com.expertsight.app.lttc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.expertsight.app.lttc.model.Member;

import com.expertsight.app.lttc.ui.BottomNavigationViewHelper;
import com.expertsight.app.lttc.util.CredentialCheckAsyncTask;

import com.expertsight.app.lttc.util.MifareHelper;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HomeActivity extends AppCompatActivity implements AdminBottomSheetDialogFragment.AdminBottomSheetDialogListener, CredentialCheckAsyncTask.CredentialCheckListener {

    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUM = 0;
    private Context context = HomeActivity.this;

    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final double FEE_PER_DAY = 5f;


    private NfcAdapter mNfcAdapter = null;

    private FirebaseDatabase db;

    private List<Member> adminList = new ArrayList<>();


    @BindView(R.id.btnAdminCredential)
    Button btnAdminCredential;

    @BindView(R.id.ivHero)
    ImageView ivHero;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupBottomNavigationView();

        db = FirebaseDatabase.getInstance();

        ButterKnife.bind(this);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Give a warning
            Toast.makeText(this, getString(R.string.msg_no_ncf), Toast.LENGTH_LONG).show();

        } else {
            if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(this, getString(R.string.msg_nfc_disabled), Toast.LENGTH_LONG).show();
            }
        }

        Glide.with(this)
                .load(R.drawable.pingpong)
                .into(ivHero);

        setupAdminList();

        // this is Daniel Knapp's id in realtime database on expertsight
        //testAdminDialog("tYPrlEVr5UmoqfvJiCz6");
        handleIntent(getIntent());
    }

    private void setupAdminList() {
        DatabaseReference ref = db.getReference("members");
        Query query = ref.orderByChild("isAdmin").equalTo(true);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                adminList.clear();
                for (DataSnapshot memberSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, memberSnapshot.getKey() + " => " + memberSnapshot.getValue());
                    Member member = memberSnapshot.getValue(Member.class);
                    member.setId(memberSnapshot.getKey());
                    adminList.add(member);
                    Log.d(TAG, "onDataChange: admin member " + member.getFullName());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: no members in list");
            }
        });
    }

    @OnClick(R.id.btnAdminCredential)
    public void validateAdminCredential() {
        Log.d(TAG, "validateAdminCredential: adminList size " + adminList.size());
        TextInputEditText etCredential = findViewById(R.id.etAdminCredential);
        String credential = etCredential.getText().toString();

        if (!credential.isEmpty()) {
            new CredentialCheckAsyncTask(adminList, this).execute(credential);
        }

    }
    
    public void applyAdminValidation(Member admin) {
        if (admin != null) {
            showAdminDialog(admin);
        } else {
            Toast.makeText(context, "Sorry, credentials not valid", Toast.LENGTH_SHORT).show();
        }
    }

    public void showAdminDialog(Member member) {
        Log.d(TAG, "showAdminDialog: start ");

        Bundle args = new Bundle();
        args.putString("member_id", member.getId());

        FragmentManager manager = getSupportFragmentManager();
        Fragment frag = manager.findFragmentByTag("fragment_admin_dialog");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        AdminBottomSheetDialogFragment adminDialogFragment = new AdminBottomSheetDialogFragment();
        adminDialogFragment.setArguments(args);
        adminDialogFragment.show(manager, "fragment_admin_dialog");

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcAdapter != null) {
            setupForegroundDispatch(this, mNfcAdapter);
        }

    }

    @Override
    protected void onPause() {

        if (mNfcAdapter != null) {
            stopForegroundDispatch(this, mNfcAdapter);
        }

        super.onPause();
    }


    @Override
    protected void onNewIntent(Intent intent) {

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.d(TAG, "handleIntent: entry");

        String action = intent.getAction();

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {



            //clear itent because it will re-run on orientation change. card should be scanned only once
            intent.setAction("");

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.d(TAG, "handleIntent: tag" + tag);

            byte[] id = tag.getId();

            final String hexId = MifareHelper.getHexString(id, id.length);
            Log.d(TAG, "handleIntent: tag ID in HEX " + hexId);

            final Query query = db.getReference("/members/")
                    .equalTo("smartcardId", hexId);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() == 1) {
                        Member member = new Member();
                        for (DataSnapshot memberSnapshot: dataSnapshot.getChildren()) {
                            member = memberSnapshot.getValue(Member.class);
                            member.setId(memberSnapshot.getKey());
                        }

                        Log.d(TAG, "onSuccess getting member by smartcard id " + member.getSmartcardId() + ": " + member.toString());
                        if (member.getIsAdmin() == true) {
                            Log.d(TAG, "onComplete: member is admin");
                            //toastTotalBalance();
                            showAdminDialog(member);
                        } else {
                            // TODO: INTENT TO LAUNCH FRAGMENT CHECKIN AND START DIALOG
                            //showCheckInMemberDialog(member);
                        }
                    } else if (dataSnapshot.getChildrenCount()  > 1){
                        Toast.makeText(context, "Error: The smartcard ID " + hexId + " is assigned to more than one member", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Couldn't find a member with the smartcard ID " + hexId, Toast.LENGTH_LONG).show();
                        // TODO: INTENT TO LAUNCH FRAGMENT CHECKIN AND START DIALOG
                        //showAddMemberDialog(hexId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: " + databaseError.toException());
                }
            });

        }
    }


    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }


    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }


    @Override
    public void applyAdminDialogData(String memberId, int buttonSelection) {
        if (buttonSelection == R.id.btnCheckIn) {

            final DatabaseReference memberRef = db.getReference("members").child(memberId);
            memberRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "onComplete:  DocumentSnapshot data: " + dataSnapshot.getValue());
                        final Member member = dataSnapshot.getValue(Member.class);
                        // TODO: INTENT TO LAUNCH FRAGMENT CHECKIN AND START DIALOG
                        //showCheckInMemberDialog(member);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: " + databaseError.toException());
                }
              });




        } else if (buttonSelection == R.id.btnAdmin) {
            Log.d(TAG, "applyAdminDialogData: memberId " + memberId);
            Intent intent = new Intent(context, AdminActivity.class);
            intent.putExtra("adminMemberId" , memberId);
            startActivity(intent);
        }
    }


    public void testAdminDialog(String memberId) {
        final DatabaseReference memberRef = db.getReference("members").child(memberId);
        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onComplete:  DocumentSnapshot data: " + dataSnapshot.getValue());
                    final Member member = dataSnapshot.getValue(Member.class);
                    member.setId(dataSnapshot.getKey());
                    showAdminDialog(member);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
