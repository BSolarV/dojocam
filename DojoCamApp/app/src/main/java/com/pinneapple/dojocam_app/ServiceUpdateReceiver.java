package com.pinneapple.dojocam_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;



public class ServiceUpdateReceiver extends BroadcastReceiver {
    /*private String RefreshTask.REFRESH_DATA_INTENT*/
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("RefreshTask.REFRESH_DATA_INTENT")) {
            // Do stuff - maybe update my view based on the changed DB contents
            Toast.makeText(context, "Weeeeeeena Men", Toast.LENGTH_SHORT).show();
        }
    }
}
