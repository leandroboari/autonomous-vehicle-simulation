package com.leandroboari.autonomousvehiclesimulation;

import android.os.Bundle;
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
