package org.unicauca.dlnamobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

public class SplashActivity extends Activity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private Handler handler;
    private Runnable runnable;
    private static final long SPLASH_MILLIS = 3000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash_layout);
        final Context context = this;
        runnable = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Intent intent = new Intent(context, SearchActivity.class);
                startActivity(intent);
                finish();
            }
        };
        handler = new Handler();
        handler.postDelayed(runnable, SPLASH_MILLIS);
        Log.i(TAG, "Pending transition scheduled to " + SPLASH_MILLIS);

    }


    @Override
    public void onBackPressed() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
            Log.i(TAG, "Pending transition removed");
        }
        super.onBackPressed();
    }

}

