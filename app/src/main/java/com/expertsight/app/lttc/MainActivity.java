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


// Main activity for app start is now CheckIn, see AndroidManifest.xml

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
