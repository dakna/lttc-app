package com.expertsight.app.lttc.util;

import android.os.AsyncTask;
import android.util.Log;

import com.expertsight.app.lttc.model.Member;
import java.util.List;

public class CredentialCheckAsyncTask extends AsyncTask<String, Void, Member> {
    private static final String TAG = "CredentialCheckAsyncTas";

    private CredentialCheckListener listener;

    public interface  CredentialCheckListener {
        void applyAdminValidation(Member admin);
    }

    private List<Member> adminList;

    public CredentialCheckAsyncTask(List<Member> adminList, CredentialCheckListener listener)
    {
        this.adminList = adminList;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Member member) {
        super.onPostExecute(member);
        listener.applyAdminValidation(member);
    }

    @Override
    protected Member doInBackground(String... strings) {

        int inputPin;

        try {
            inputPin = Integer.parseInt(strings[0]);
        } catch (NumberFormatException e) {
            return null;
        }

        for (Member member: adminList) {
            Log.d(TAG, "doInBackground: checking member " + member.getFullName());
            if (member.getPin() == inputPin) return member;
        }
        return null;
    }
}
