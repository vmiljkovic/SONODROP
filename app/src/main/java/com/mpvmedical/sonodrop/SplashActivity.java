package com.mpvmedical.sonodrop;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    boolean mToGo = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        mHandler.postDelayed(runnable, 2000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mToGo) {
                SplashActivity.this.finish();
                Intent intent = new Intent(getBaseContext(), DeviceListActivity.class);
                startActivity(intent);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        mToGo = false;
        mHandler.removeCallbacks(runnable);
    }
}
