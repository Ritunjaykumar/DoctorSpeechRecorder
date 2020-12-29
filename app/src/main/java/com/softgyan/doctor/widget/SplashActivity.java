package com.softgyan.doctor.widget;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.softgyan.doctor.R;
import com.softgyan.doctor.util.UserInfo;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        startNewActivity();
    }

    private void startNewActivity() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!UserInfo.getInstance(SplashActivity.this).isLogin()) {
                    Intent accountActivity = new Intent(SplashActivity.this, AccountActivity.class);
                    accountActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(accountActivity);
                    finish();
                }else{
                    Intent startMain = new Intent(SplashActivity.this, MainActivity.class);
                    startMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startMain);
                }
            }
        }, 1000);
    }
}