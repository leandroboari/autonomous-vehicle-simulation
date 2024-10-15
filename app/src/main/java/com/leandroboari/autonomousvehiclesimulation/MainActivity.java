package com.leandroboari.autonomousvehiclesimulation;

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

        buttonControl = findViewById(R.id.button_control);
        buttonFinish = findViewById(R.id.button_finish);
        inputCars = findViewById(R.id.input_cars);
        textViewInfo = findViewById(R.id.text_info);

        gameView = findViewById(R.id.game_view);
        gameView.setTextViewInfo(textViewInfo);
        gameView.init(this);
        gameView.resume();

        // Adiciona um TextWatcher para escutar alterações no campo de input_cars
        inputCars.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String carsInput = editable.toString();
                if (!carsInput.isEmpty()) {
                    int numCars = Integer.parseInt(carsInput);
                    if(numCars == 0 || numCars > 8) {
                        Toast.makeText(
                                MainActivity.this,
                                "Insira um número de 1 a 8.",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else if (gameView != null) {
                        gameView.createCarPlaceholders(numCars);
                        buttonControl.setVisibility(View.VISIBLE);
                    } else {
                        buttonControl.setVisibility(View.GONE);
                    }
                }
            }
        });

        buttonControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Remove o foco do campo inputCars
                inputCars.clearFocus();

                // Esconde o teclado
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                if(!gameView.isGameStarted()) {
                    inputCars.setVisibility(View.GONE);
                    buttonFinish.setVisibility(View.VISIBLE);
                }

                gameView.togglePause();

                // Atualiza o texto do botão
                if (gameView.isGamePaused()) {
                    buttonControl.setText("Play");
                    buttonControl.setBackgroundColor(getResources().getColor(R.color.green));
                } else {
                    buttonControl.setText("Pausar");
                    buttonControl.setBackgroundColor(getResources().getColor(R.color.yellow));
                }
            }
        });

        buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameView.togglePause();
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!gameView.isGamePaused()) {
            gameView.resume();  // Retoma o jogo
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pause();  // Pausa o jogo
        }
    }
}
