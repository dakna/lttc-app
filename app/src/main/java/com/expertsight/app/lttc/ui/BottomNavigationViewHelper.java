package com.expertsight.app.lttc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.expertsight.app.lttc.AdminActivity;
import com.expertsight.app.lttc.MainActivity;
import com.expertsight.app.lttc.HistoryActivity;
import com.expertsight.app.lttc.HomeActivity;
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
                //default
                Intent intent = new Intent(context, HomeActivity.class);

                switch (item.getItemId()){

                    case R.id.ic_home:
                        intent = new Intent(context, HomeActivity.class);//ACTIVITY_NUM = 0
                        break;

                    case R.id.ic_play:
                        intent = new Intent(context, PlayActivity.class);//ACTIVITY_NUM = 1
                        break;

                    case R.id.ic_member:
                        intent = new Intent(context, AdminActivity.class);//ACTIVITY_NUM = 2
                        break;

                }

                context.startActivity(intent);
                callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);


                return false;
            }
        });
    }
}
