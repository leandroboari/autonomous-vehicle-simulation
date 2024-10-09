package com.leandroboari.autonomousvehiclesimulation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Infla o layout XML em vez de usar o GameView diretamente
        setContentView(R.layout.activity_main);

        // Recupera a referência ao GameView do layout
        gameView = findViewById(R.id.game_view);

        // Inicializa o GameView (isso pode ser feito no GameView diretamente)
        gameView.init(this);

        Button buttonControl = findViewById(R.id.button_control);
        buttonControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameView.togglePause(); // Alterna o estado de pausa

                if (gameView.isGamePaused()) {
                    buttonControl.setText("Começar");
                } else {
                    buttonControl.setText("Pausar");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
}
