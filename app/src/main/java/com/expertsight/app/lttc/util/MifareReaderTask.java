package com.expertsight.app.lttc.util;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

public class MifareReaderTask extends AsyncTask<Tag, Void, String> {
    private static final String TAG = "MifareReaderTask";

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
            Log.d(TAG, "onPostExecute: found tag result" + result);
        }
    }

}
