package com.leandroboari.autonomousvehiclesimulation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;
    private EditText inputCars;
    private Button buttonControl;
    private Button buttonFinish;
    TextView textViewInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialização dos componentes da UI
        buttonControl = findViewById(R.id.button_control);
        buttonFinish = findViewById(R.id.button_finish);
        inputCars = findViewById(R.id.input_cars);
        textViewInfo = findViewById(R.id.text_info);

        // Configurações iniciais do gameView
        gameView = findViewById(R.id.game_view);
        gameView.setTextViewInfo(textViewInfo);

        // Inicializa o GameView
        gameView.init(this);

        // Retoma o jogo se já estiver pausado
        gameView.resume();

        // Adiciona um TextWatcher para capturar alterações no campo de entrada de carros
        inputCars.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Sem ação necessária aqui
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Sem ação necessária aqui
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Após a mudança de texto no inputCars
                String carsInput = editable.toString();
                if (!carsInput.isEmpty()) {
                    int numCars = Integer.parseInt(carsInput);

                    // Validação do número de carros
                    if (numCars == 0 || numCars > 8) {
                        Toast.makeText(MainActivity.this, "Insira um número de 1 a 8.", Toast.LENGTH_SHORT).show();
                    } else if (gameView != null) {
                        // Cria os placeholders de carros com base no número fornecido
                        gameView.createCarPlaceholders(numCars);

                        // Exibe o botão de controle
                        buttonControl.setVisibility(View.VISIBLE);
                    } else {

                        // Oculta o botão se não for válido
                        buttonControl.setVisibility(View.GONE);
                    }
                }
            }
        });

        // Configuração do botão de controle (Play/Pause)
        buttonControl.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                // Remove o foco do campo inputCars e esconde o teclado
                inputCars.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                if (!gameView.isGameStarted()) {
                    // Oculta a entrada de carros após início do jogo
                    inputCars.setVisibility(View.GONE);

                    // Exibe o botão de finalização
                    buttonFinish.setVisibility(View.VISIBLE);
                }

                // Alterna entre pausa e retomada do jogo
                gameView.togglePause();

                // Atualiza o texto e cor do botão de controle baseado no estado do jogo
                if (gameView.isGamePaused()) {
                    buttonControl.setText("Play");
                    buttonControl.setBackgroundColor(getResources().getColor(R.color.green));
                } else {
                    buttonControl.setText("Pausar");
                    buttonControl.setBackgroundColor(getResources().getColor(R.color.yellow));
                }
            }
        });

        // Configuração do botão de finalização
        buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameView.endGame();

                // Finaliza a atividade
                finish();
            }
        });
    }

    // Métodos do ciclo de vida da Activity para gerenciar o estado do jogo

    @Override
    protected void onResume() {
        super.onResume();
        // Retoma o jogo se ele não estiver pausado
        if (!gameView.isGamePaused()) {
            gameView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausa o jogo ao pausar a Activity
        if (gameView != null) {
            gameView.pause();
        }
    }
}