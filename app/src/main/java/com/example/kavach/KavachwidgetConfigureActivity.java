package com.example.kavach;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;

public class KavachwidgetConfigureActivity extends Activity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            int mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);


            // Updated the widget immediately after initialization
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            Kavachwidget.updateAppWidget(this, appWidgetManager, mAppWidgetId);

            // Set the result to indicate success and finish the activity
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        } else {
            finish();
        }
    }
}
