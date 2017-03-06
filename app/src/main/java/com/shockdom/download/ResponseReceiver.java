package com.shockdom.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class ResponseReceiver extends BroadcastReceiver {
   public static final String ACTION_RESP = "com.shockdom.smartcomix.intent.action.ACTION_RESP";

    /** call me in onResume() **/
    public static void register(Context c, ResponseReceiver receiver) {
        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        c.registerReceiver(receiver, filter);
    }

    /** call me in onPause() **/
    public static void unregister(Context c, ResponseReceiver receiver) {
        c.unregisterReceiver(receiver);
    }
    
   @Override
    public void onReceive(Context context, Intent intent) {
       String volume = intent.getStringExtra(DownloaderService.PARAM_VOLUME_ID);
       if (intent.hasExtra(DownloaderService.PARAM_IS_COMPLETE)) {
           onComplete(volume);
       }
       else if (intent.hasExtra(DownloaderService.PARAM_ERROR)) {
           String error = intent.getStringExtra(DownloaderService.PARAM_ERROR);
           onError(volume, error);
       }
       else {
           int percent = intent.getIntExtra(DownloaderService.PARAM_PERCENTAGE, 0);
           onProgress(volume, percent);
       }
    }

    public abstract void onProgress(String volume_id, int percentage);
    public abstract void onComplete(String volume_id);
    public abstract void onError(String volume_id, String error);

}