package com.expertsight.app.lttc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.nfc.tech.Ndef;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.expertsight.app.lttc.ui.BottomNavigationViewHelper;
import com.expertsight.app.lttc.util.MifareHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ACTIVITY_NUM = 2;
    private Context context = MainActivity.this;

    public static final String MIME_TEXT_PLAIN = "text/plain";

    private TextView mTextView = null;
    private NfcAdapter mNfcAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupBottomNavigationView();

        mTextView = (TextView) findViewById(R.id.tv_nfc);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;

        }

        if (!mNfcAdapter.isEnabled()) {
            mTextView.setText("NFC is disabled.");
        } else {
            mTextView.setText("NFC is enabled for MifareClassic");
        }

        handleIntent(getIntent());
    }


    @Override
    protected void onResume() {
        super.onResume();

        /*
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /*
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        /*
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.d(TAG, "handleIntent: entry");

        String action = intent.getAction();
        mTextView.setText("handling intent with action" + action);

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {



            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.d(TAG, "handleIntent: tag" + tag);

            byte[] id = tag.getId();
            Log.d(TAG, "handleIntent: tag ID in HEX " + MifareHelper.getHexString(id, id.length));

            mTextView.setText(MifareHelper.getHexString(id, id.length));

/*            String[] techList = tag.getTechList();
            String searchedTech = MifareClassic.class.getName();
            Log.d(TAG, "handleIntent: searched tech:" + searchedTech);
            for (String tech : techList) {
                Log.d(TAG, "handleIntent: techlist:" + tech);
                if (searchedTech.equals(tech)) {
                    new MifareReaderTask().execute(tag);
                    break;
                }
            }*/
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


    private class MifareReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Log.d(TAG, "doInBackground: entry");
            Tag tag = params[0];

            byte[] id = tag.getId();
            Log.d(TAG, "doInBackground: tag ID in HEX " + MifareHelper.getHexString(id, id.length));
            MifareClassic mifareClassic = MifareClassic.get(tag);
            if (mifareClassic == null) {
                // mifareClassic is not supported by this Tag.
                Log.d(TAG, "doInBackground: mifareClassic not supported");
                return null;
            }

            byte[] data;

            try {       //  5.1) Connect to card
                mifareClassic.connect();
                boolean auth = false;
                String cardData = null;
                // 5.2) and get the number of sectors this card has..and loop thru these sectors
                int secCount = mifareClassic.getSectorCount();
                int bCount = 0;
                int bIndex = 0;
                for(int j = 0; j < secCount; j++){
                    // 6.1) authenticate the sector
                    auth = mifareClassic.authenticateSectorWithKeyA(j, MifareClassic.KEY_DEFAULT);
                    if(auth){
                        // 6.2) In each sector - get the block count
                        bCount = mifareClassic.getBlockCountInSector(j);
                        bIndex = 0;
                        for(int i = 0; i < bCount; i++){
                            bIndex = mifareClassic.sectorToBlock(j);
                            // 6.3) Read the block
                            data = mifareClassic.readBlock(bIndex);
                            // 7) Convert the data into a string from Hex format.
                            cardData = cardData + MifareHelper.getHexString(data, data.length);
                            Log.i(TAG, MifareHelper.getHexString(data, data.length));
                            bIndex++;
                        }
                        return cardData;
                    }else{ // Authentication failed - Handle it
                        Log.d(TAG, "doInBackground: mifare not authenticated");
                    }
                }
            }catch (IOException e) {
                Log.e(TAG, "doInBackground: exception " + Log.getStackTraceString(e));

            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                mTextView.setText("Read content: " + result);
            }
        }

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
