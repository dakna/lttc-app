package com.expertsight.app.lttc.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.expertsight.app.lttc.model.Member;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class Store {
    private static final String TAG = "Store";
    private static final String SHAREDPREFERENCES_NAME = "member_list_sharedpreferences";
    private static final String MEMBER_LIST_KEY = "member_list_key";

    public static void saveMemberList(Context context, List<Member> memberList) {
        Log.d(TAG, "saveRecipe: ");
        SharedPreferences.Editor prefs = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String jsonRecipe = gson.toJson(memberList);
        prefs.putString(MEMBER_LIST_KEY, jsonRecipe);
        prefs.apply();
    }

    public static List<Member> loadMemberList(Context context) {
        Log.d(TAG, "loadRecipe: ");
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        String jsonRecipe = prefs.getString(MEMBER_LIST_KEY, "");
        Gson gson = new Gson();
        List<Member> parsedList = gson.fromJson(prefs.getString(MEMBER_LIST_KEY, ""), new TypeToken<List<Member>>(){}.getType());
        return "".equals(jsonRecipe) ? null : parsedList;
    }

}
