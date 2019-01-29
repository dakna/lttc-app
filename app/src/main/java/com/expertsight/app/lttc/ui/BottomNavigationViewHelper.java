package com.expertsight.app.lttc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.expertsight.app.lttc.AdminActivity;
import com.expertsight.app.lttc.AdminOldActivity;
import com.expertsight.app.lttc.MainActivity;
import com.expertsight.app.lttc.HistoryActivity;
import com.expertsight.app.lttc.CheckInActivity;
import com.expertsight.app.lttc.PlayActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.expertsight.app.lttc.R;



public class BottomNavigationViewHelper {

    private static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx){
        Log.d(TAG, "setupBottomNavigationView: Setting up BottomNavigationView");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
        //bottomNavigationViewEx.setIconSize(R.dimen.bottom_nav_iconsize, R.dimen.bottom_nav_iconsize);

        Log.d(TAG, "setupBottomNavigationView: icon measured height" + bottomNavigationViewEx.getIconAt(1).getMeasuredHeight());
        //bottomNavigationViewEx.setItemHeight(150);
    }

    public static void enableNavigation(final Context context, final Activity callingActivity, BottomNavigationViewEx view){

        float iconSize = context.getResources().getInteger(R.integer.bottom_nav_iconsize);
        view.setIconSize(iconSize, iconSize);
        view.setItemHeight(BottomNavigationViewEx.dp2px(context,iconSize + 10));

        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){

                    case R.id.ic_home:
                        Intent intent1 = new Intent(context, MainActivity.class);//ACTIVITY_NUM = 0
                        context.startActivity(intent1);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.ic_checkin:
                        Intent intent2 = new Intent(context, CheckInActivity.class);//ACTIVITY_NUM = 1
                        context.startActivity(intent2);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.ic_member:
                        Intent intent3 = new Intent(context, AdminActivity.class);//ACTIVITY_NUM = 2
                        context.startActivity(intent3);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.ic_history:
                        Intent intent4 = new Intent(context, HistoryActivity.class);//ACTIVITY_NUM = 3
                        context.startActivity(intent4);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.ic_play:
                        Intent intent5 = new Intent(context, PlayActivity.class);//ACTIVITY_NUM = 4
                        context.startActivity(intent5);
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                }


                return false;
            }
        });
    }
}
