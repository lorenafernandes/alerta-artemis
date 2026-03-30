package com.example.alertartemis;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
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

import java.util.ArrayList;

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Permissões necessárias não concedidas.", Toast.LENGTH_LONG).show();
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 1. Verifica se GPS está ligado
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Ative o GPS para enviar sua localização.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            // feedback visual imediato
            Toast.makeText(this, "Iniciando Protocolo Artemis. Localizando...", Toast.LENGTH_SHORT).show();

            // instancia o cliente do google
            com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient =
                    com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this);

            // localização atual
            fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    null
            ).addOnSuccessListener(this, location -> {
                if (location != null) {
                    enviarSmsComLocalizacao(location);
                } else {
                    // Tenta a última conhecida se a atual falhar
                    fusedLocationClient.getLastLocation().addOnSuccessListener(lastLoc -> {
                        if (lastLoc != null) {
                            enviarSmsComLocalizacao(lastLoc);
                        } else {
                            // envia alerta básico sem GPS
                            String msg = "ALERTA ARTEMIS! Preciso de ajuda urgente! (Localização exata indisponível).";
                            enviarSmsParaRedeDeApoio(msg);
                            Toast.makeText(this, "Localização não encontrada. Enviando alerta básico...", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Falha no sensor de localização.", Toast.LENGTH_SHORT).show();
            });

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void enviarSmsComLocalizacao(Location location) {

        String urlMaps = "https://maps.google.com/?q="
                + location.getLatitude() + ","
                + location.getLongitude();

        String mensagem = "ALERTA ARTEMIS. Preciso de ajuda!\nMinha localização:\n" + urlMaps;

        enviarSmsParaRedeDeApoio(mensagem);
    }
    private void enviarSmsParaRedeDeApoio(String mensagemPronta) {

        SharedPreferences bancoDeDados = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SmsManager gerenciadorDeSms;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            gerenciadorDeSms = this.getSystemService(SmsManager.class);
        } else {
            // Para versões antigas
            gerenciadorDeSms = SmsManager.getDefault();
        }

        String[] telefones = {
                bancoDeDados.getString("tel1", ""),
                bancoDeDados.getString("tel2", ""),
                bancoDeDados.getString("tel3", "")
        };

        int contatosAcionados = 0;

        for (String telefone : telefones) {

            if (telefone != null && !telefone.trim().isEmpty()) {

                String numeroLimpo = telefone.replaceAll("[^0-9]", "");

                if (!numeroLimpo.startsWith("55")) {
                    numeroLimpo = "55" + numeroLimpo;
                }

                try {
                    ArrayList<String> partes = gerenciadorDeSms.divideMessage(mensagemPronta);
                    gerenciadorDeSms.sendMultipartTextMessage(numeroLimpo, null, partes, null, null);
                    contatosAcionados++;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (contatosAcionados > 0) {
            Toast.makeText(this, "Socorro enviado!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Nenhum contato salvo.", Toast.LENGTH_LONG).show();
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