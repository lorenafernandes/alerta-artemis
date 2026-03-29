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

        // CLEAN CODE: 1. Pegamos O CONJUNTO COMPLETO (Círculo + Arco) pelo ID do Layout
        View layoutLogo = findViewById(R.id.layoutLogoCompleto);

        // CLEAN CODE: 2. Carregamos o arquivo de animação 'floating.xml' que criamos ontem
        Animation flutuacao = AnimationUtils.loadAnimation(this, R.anim.floating);

        // CLEAN CODE: 3. Damos o "play" na animação NO CONJUNTO INTEIRO
        if (layoutLogo != null) {
            layoutLogo.startAnimation(flutuacao);
        }

        // O timer de 3 segundos (3000ms) para pular de tela continua o mesmo
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Cria a intenção de mudar para a MainActivity (Tela do Botão)
                Intent intencaoDeMudarDeTela = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intencaoDeMudarDeTela);

                // Finaliza a Splash para a usuária não voltar para ela se apertar "Voltar"
                finish();
            }
        }, 3000);
    }
}