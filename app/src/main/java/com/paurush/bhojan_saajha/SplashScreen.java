package com.paurush.bhojan_saajha;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen); // Set your layout

        // Delay for a few seconds
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreen.this, LandingPage.class));
            finish();
        }, 3000); // 2000 milliseconds (2 seconds)
    }
}
