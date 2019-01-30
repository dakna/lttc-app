package com.expertsight.app.lttc.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViewsService;

import com.expertsight.app.lttc.model.Member;

import java.util.List;


public class MemberListWidgetService extends RemoteViewsService {

    public static void updateWidget(Context context, List<Member> memberList) {
        Store.saveMemberList(context, memberList);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, MemberListWidgetProvider.class));
        MemberListWidgetProvider.updateRecipeWidgets(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        return new MemberListRemoteViewsFactory(getApplicationContext());
    }

}