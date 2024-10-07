package com.leandroboari.autonomousvehiclesimulation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {

    private Car car;
    private Track track;
    private Thread gameThread;
    private SurfaceHolder surfaceHolder;
    private boolean running = false;
    private PIDController pidController;
    private long startTime;

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        surfaceHolder = getHolder();

        // Inicializa o tempo de início
        startTime = System.currentTimeMillis();

        // Carrega a pista e define a linha de largada/chegada
        track = new Track(context, R.drawable.pista, 573, 404, 627, 478); // Exemplo de uso com uma imagem de pista

        // Inicia o carro e o controlador PID
        pidController = new PIDController(0.8f, 0.01f, 0.1f);
        car = new Car(this, 400, 400, 0, 5, pidController); // Inicia o carro em uma posição fixa
    }

    @Override
    public void run() {
        while (running) {
            if (surfaceHolder.getSurface().isValid()) {
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawRGB(255, 255, 255); // Fundo branco

                // Desenha a pista e a linha de largada
                track.draw(canvas);

                // Desenha o carro
                car.draw(canvas);

                // Desenha as informações sobre o carro (tempo, distância, voltas)
                drawInfo(canvas);

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    // Função para desenhar as informações na tela
    private void drawInfo(Canvas canvas) {
        Paint infoPaint = new Paint();
        infoPaint.setColor(Color.GRAY);
        infoPaint.setTextSize(40);

        // Calcula e desenha as informações
        long currentTime = System.currentTimeMillis();
        float elapsedTime = (currentTime - startTime) / 1000f; // Tempo decorrido em segundos
        float distanceMoved = car.getTotalDistanceMoved(); // Distância total percorrida pelo carro
        int laps = car.getLapCount(); // Número de voltas

        // Exibe as informações na tela
        canvas.drawText("Tempo: " + elapsedTime + "s", 10, 50, infoPaint);
        canvas.drawText("Distância: " + distanceMoved + "px", 10, 100, infoPaint);
        canvas.drawText("Voltas: " + laps, 10, 150, infoPaint);
        canvas.drawText("Velocidade: " + car.getSpeed() + "px/s", 10, 200, infoPaint);
    }

    // Função para retomar o jogo
    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
        car.start(); // Inicia a thread do carro
    }

    // Função para pausar o jogo
    public void pause() {
        running = false;
        car.stopCar(); // Para a thread do carro
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Verifica se o carro está colidindo com algo na pista
    public boolean isCollision(float x, float y) {
        // Verifica se o carro está colidindo com a pista
        return track.isCollision(x, y);
    }
}
