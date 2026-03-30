package com.example.alertartemis;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    // Cronômetro de 3 segundos
    private Handler cronometro = new Handler(Looper.getMainLooper());
    private Runnable acaoEmergencia;
    private MediaPlayer tocadorDeSom;

    // Constantes de Segurança
    private static final int PERMISSAO_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "ArtemisDatabase";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Pede permissões assim que a tela abre
        solicitarPermissoesDeSeguranca();

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

            // ícone do telefone abre a Tela 3
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

        // Toca a sirene
        tocadorDeSom = MediaPlayer.create(this, R.raw.sirene);
        tocadorDeSom.start();

        // Integração de Resgate (GPS + SMS)
        executarProtocoloDeResgate();
    }

    // MÉTODOS DE ENGENHARIA DO ALERTA

    private void executarProtocoloDeResgate() {
        // 1. Inicializa o locationManager buscando o serviço de localização do sistema
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissões negadas. Não é possível enviar o SMS de socorro.", Toast.LENGTH_LONG).show();
            return;
        }

        Location localizacao = null;

        try {
            // Tenta obter a localização pelo GPS
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                localizacao = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            // Se falhar, tenta pela Rede (Torres/Wi-Fi)
            if (localizacao == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                localizacao = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        String mensagem = "🚨 ALERTA ARTEMIS: Preciso de ajuda imediata! Acompanhe minha localizacao exata no mapa abaixo:\n";

        if (localizacao != null) {
            mensagem += "https://maps.google.com/?q=" + localizacao.getLatitude() + "," + localizacao.getLongitude();
        } else {
            mensagem += "(Sinal de GPS indisponivel no momento. Tente me ligar).";
        }
    }

    private void enviarSmsParaRedeDeApoio(String mensagemPronta) {
        SharedPreferences bancoDeDados = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SmsManager gerenciadorDeSms = SmsManager.getDefault();

        // Recupera os 3 contatos da Tela 3
        String[] telefones = {
                bancoDeDados.getString("tel1", ""),
                bancoDeDados.getString("tel2", ""),
                bancoDeDados.getString("tel3", "")
        };

        int contatosAcionados = 0;

        // Dispara o SMS
        for (String telefone : telefones) {
            if (telefone != null && !telefone.trim().isEmpty()) {

                // Limpa o número (remove parênteses, traços e espaços)
                String numeroLimpo = telefone.replaceAll("[^0-9]", "");

                // Força o código do Brasil se a pessoa não tiver digitado
                if (!numeroLimpo.startsWith("55")) {
                    numeroLimpo = "+55" + numeroLimpo;
                } else {
                    numeroLimpo = "+" + numeroLimpo;
                }

                try {
                    gerenciadorDeSms.sendTextMessage(numeroLimpo, null, mensagemPronta, null, null);
                    contatosAcionados++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (contatosAcionados > 0) {
            Toast.makeText(this, "Socorro enviado para " + contatosAcionados + " contato(s)!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Nenhum contato salvo na Rede de Apoio.", Toast.LENGTH_LONG).show();
        }
    }

    private void solicitarPermissoesDeSeguranca() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSAO_REQUEST_CODE);
        }
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