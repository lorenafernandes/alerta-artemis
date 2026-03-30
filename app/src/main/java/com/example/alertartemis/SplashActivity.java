package com.example.alertartemis;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View layoutLogo = findViewById(R.id.layoutLogoCompleto);
        Animation flutuacao = AnimationUtils.loadAnimation(this, R.anim.floating);
        if (layoutLogo != null) {
            layoutLogo.startAnimation(flutuacao);
        }

        // timer de 3sec
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // intenção de mudar para a MainActivity
                Intent intencaoDeMudarDeTela = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intencaoDeMudarDeTela);
                finish();
            }
        }, 3000);
    }
}