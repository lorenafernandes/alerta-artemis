package com.example.alertartemis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ContatosActivity extends AppCompatActivity {

    private EditText inputNome1, inputTel1, inputNome2, inputTel2, inputNome3, inputTel3;
    private Button btnSalvarContatos;

    // Variáveis do Accordion (Sanfona)
    private LinearLayout headerContato1, headerContato2, headerContato3;
    private LinearLayout bodyContato1, bodyContato2, bodyContato3;
    private TextView txtSeta1, txtSeta2, txtSeta3;

    // Controla qual aba está aberta (-1 significa nenhuma)
    private int contatoAberto = -1;

    private static final String PREFS_NAME = "ArtemisDatabase";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contatos);

        // Conectando os inputs
        inputNome1 = findViewById(R.id.inputNome1);
        inputTel1 = findViewById(R.id.inputTel1);
        inputNome2 = findViewById(R.id.inputNome2);
        inputTel2 = findViewById(R.id.inputTel2);
        inputNome3 = findViewById(R.id.inputNome3);
        inputTel3 = findViewById(R.id.inputTel3);
        btnSalvarContatos = findViewById(R.id.btnSalvarContatos);

        // Conectando os elementos do Accordion
        headerContato1 = findViewById(R.id.headerContato1);
        headerContato2 = findViewById(R.id.headerContato2);
        headerContato3 = findViewById(R.id.headerContato3);
        bodyContato1 = findViewById(R.id.bodyContato1);
        bodyContato2 = findViewById(R.id.bodyContato2);
        bodyContato3 = findViewById(R.id.bodyContato3);
        txtSeta1 = findViewById(R.id.txtSeta1);
        txtSeta2 = findViewById(R.id.txtSeta2);
        txtSeta3 = findViewById(R.id.txtSeta3);

        carregarContatosOffline();

        // Configurando os cliques nas abas
        headerContato1.setOnClickListener(v -> alternarAccordion(1));
        headerContato2.setOnClickListener(v -> alternarAccordion(2));
        headerContato3.setOnClickListener(v -> alternarAccordion(3));

        btnSalvarContatos.setOnClickListener(v -> salvarContatosOffline());

        FrameLayout btnNavHome = findViewById(R.id.btnNavHome);
        btnNavHome.setOnClickListener(v -> {
            Intent voltarParaHome = new Intent(ContatosActivity.this, MainActivity.class);
            startActivity(voltarParaHome);
            finish();
        });
    }

    // A mágica que abre um e fecha os outros
    private void alternarAccordion(int contatoClicado) {
        // 1. Esconde todos os corpos e vira as setas para baixo
        bodyContato1.setVisibility(View.GONE);
        bodyContato2.setVisibility(View.GONE);
        bodyContato3.setVisibility(View.GONE);
        txtSeta1.setText("▼");
        txtSeta2.setText("▼");
        txtSeta3.setText("▼");

        // 2. Abre apenas o que foi clicado (se ele já não estava aberto)
        if (contatoClicado == 1 && contatoAberto != 1) {
            bodyContato1.setVisibility(View.VISIBLE);
            txtSeta1.setText("▲");
            contatoAberto = 1;
        } else if (contatoClicado == 2 && contatoAberto != 2) {
            bodyContato2.setVisibility(View.VISIBLE);
            txtSeta2.setText("▲");
            contatoAberto = 2;
        } else if (contatoClicado == 3 && contatoAberto != 3) {
            bodyContato3.setVisibility(View.VISIBLE);
            txtSeta3.setText("▲");
            contatoAberto = 3;
        } else {
            // Se clicou na aba que já estava aberta, ela apenas fecha
            contatoAberto = -1;
        }
    }

    private void salvarContatosOffline() {
        SharedPreferences bancoDeDados = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = bancoDeDados.edit();
        editor.putString("nome1", inputNome1.getText().toString());
        editor.putString("tel1", inputTel1.getText().toString());
        editor.putString("nome2", inputNome2.getText().toString());
        editor.putString("tel2", inputTel2.getText().toString());
        editor.putString("nome3", inputNome3.getText().toString());
        editor.putString("tel3", inputTel3.getText().toString());
        editor.apply();
        Toast.makeText(this, "Contatos salvos offline!", Toast.LENGTH_SHORT).show();
    }

    private void carregarContatosOffline() {
        SharedPreferences bancoDeDados = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        inputNome1.setText(bancoDeDados.getString("nome1", ""));
        inputTel1.setText(bancoDeDados.getString("tel1", ""));
        inputNome2.setText(bancoDeDados.getString("nome2", ""));
        inputTel2.setText(bancoDeDados.getString("tel2", ""));
        inputNome3.setText(bancoDeDados.getString("nome3", ""));
        inputTel3.setText(bancoDeDados.getString("tel3", ""));
    }
}