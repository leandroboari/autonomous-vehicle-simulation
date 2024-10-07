package com.leandroboari.autonomousvehiclesimulation;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(gameView); // Define nossa custom view como o layout da atividade.
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume(); // Retomar o loop do jogo ao voltar ao app.
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause(); // Pausar o loop do jogo ao sair do app.
    }
}
