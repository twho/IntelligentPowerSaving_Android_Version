package com.tsungweiho.intelligentpowersaving.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.tsungweiho.intelligentpowersaving.MainActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by AKiniyalocts on 1/15/15.
 * <p>
 * Basic network utils
 */
public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    /**
     * Check if the internet connection is active
     *
     * @param context the context calls this method
     * @return the boolean indicate if the internet is connected
     */
    public static boolean isConnected(Context context) {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
        return false;
    }

    /**
     * Check internet connection
     */
    public void checkNetworkConnection() {
        new TaskCheckConnection().execute();
    }

    /**
     * AsyncTask for checking internet connection
     */
    private static class TaskCheckConnection extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlConnection.setRequestProperty("User-Agent", "Test");
                urlConnection.setRequestProperty("Connection", "close");
                urlConnection.setConnectTimeout(3000);
                urlConnection.connect();
                return urlConnection.getResponseCode() == 200;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (null != MainActivity.getContext())
                ((MainActivity) MainActivity.getContext()).setConnectionMessage(result);
        }
    }
}
