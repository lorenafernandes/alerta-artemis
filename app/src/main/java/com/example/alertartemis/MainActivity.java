package com.example.alertartemis;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Cronômetro de 3 segundos
    private Handler cronometro = new Handler(Looper.getMainLooper());
    private Runnable acaoEmergencia;
    private MediaPlayer tocadorDeSom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Busca do botão pelo ID
        FrameLayout btnEmergencia = findViewById(R.id.containerBotao);

        // Sensor de toque
        btnEmergencia.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        // O DEDO ENCOSTOU NA TELA
                        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                        v.setAlpha(0.8f);
                        acaoEmergencia = new Runnable() {
                            @Override
                            public void run() {
                                dispararAlarme();
                            }
                        };

                        // contagem de 3sec
                        cronometro.postDelayed(acaoEmergencia, 3000);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // O DEDO SOLTOU A TELA (ou escorregou para fora)
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        v.setAlpha(1.0f);

                        // Caso o dedo saia antes dos 3sec
                        if (acaoEmergencia != null) {
                            cronometro.removeCallbacks(acaoEmergencia);
                        }
                        return true;
                }
                return false;
            }
        });

        // LIGANDO A NAVEGAÇÃO: Faz o ícone do telefone abrir a Tela 3
        FrameLayout btnNavContatos = findViewById(R.id.btnNavContatos);
        if (btnNavContatos != null) {
            btnNavContatos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent irParaContatos = new Intent(MainActivity.this, ContatosActivity.class);
                    startActivity(irParaContatos);
                }
            });
        }
    }

    private void dispararAlarme() {
        // Feedback visual na tela
        Toast.makeText(MainActivity.this, "🚨 ALERTA DISPARADO! 🚨", Toast.LENGTH_LONG).show();
        tocadorDeSom = MediaPlayer.create(this, R.raw.sirene);
        tocadorDeSom.start();
    }

    // Libera a memória do áudio se o app for fechado
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tocadorDeSom != null) {
            tocadorDeSom.release();
        }
    }
}