package com.expertsight.app.lttc.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.expertsight.app.lttc.PlayActivity;
import com.expertsight.app.lttc.R;
import com.expertsight.app.lttc.model.Member;

import java.util.List;

public class MemberListWidgetProvider extends AppWidgetProvider {
    private static final String TAG = "MemberListWidgetProvide";

    public static void updateMemberWidget(Context context, AppWidgetManager appWidgetManager, int recipeWidgetId) {
        Log.d(TAG, "updateMemberWidget: ");
        List<Member> memberList = Store.loadMemberList(context);
        if (memberList != null) {

            Log.d(TAG, "updateMemberWidget: member list size" + memberList.size());
            Intent clickIntent =  new Intent(context, PlayActivity.class);
            clickIntent.putExtra(PlayActivity.FRAGMENT_SELECT, PlayActivity.FRAGMENT_CURRENT_WEEK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, clickIntent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

            views.setTextViewText(R.id.tv_widget_name, context.getResources().getString(R.string.headline_member_checked_in_list));
            views.setOnClickPendingIntent(R.id.tv_widget_name, pendingIntent);

            Intent intent = new Intent(context, MemberListWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, recipeWidgetId);
            views.setRemoteAdapter(R.id.lv_widget, intent);

            appWidgetManager.updateAppWidget(recipeWidgetId, views);
            appWidgetManager.notifyAppWidgetViewDataChanged(recipeWidgetId, R.id.lv_widget);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] recipeWidgetIdArray) {
        Log.d(TAG, "onUpdate: ");
        for (int recipeWidgetId : recipeWidgetIdArray) {
            updateMemberWidget(context, appWidgetManager, recipeWidgetId);
        }
    }

    public static void updateRecipeWidgets(Context context, AppWidgetManager appWidgetManager, int[] recipeWidgetIdArray) {
        Log.d(TAG, "updateRecipeWidgets: ");
        for (int recipeWidgetId : recipeWidgetIdArray) {
            updateMemberWidget(context, appWidgetManager, recipeWidgetId);
        }
    }
}

