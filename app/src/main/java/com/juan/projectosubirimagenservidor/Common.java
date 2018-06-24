package com.juan.projectosubirimagenservidor;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;


public class Common extends Application {

    protected static Context context;

    @Override
    public void onCreate() {
        this.context = getApplicationContext();
        super.onCreate();
    }

    public static Context getContext() {
        return context;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static boolean hasMemorySD() {
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        Boolean isSDSupportedDevice = Environment.isExternalStorageRemovable();

        return isSDPresent && isSDSupportedDevice;
    }
}
