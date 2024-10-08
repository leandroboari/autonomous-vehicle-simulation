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
    private long startTime, lastFrameTime;
    private int fps;

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
        lastFrameTime = System.currentTimeMillis();

        // Carrega a pista e define a linha de largada/chegada
        int trackWidth = 800;
        int trackHeight = 600;
        track = new Track(
                context,
                R.drawable.pista,
                trackWidth,
                trackHeight,
                684,
                347,
                736,
                402
        );

        // Inicia o carro
        car = new Car(
                this,
                683,
                381,
                -20,
                5
        );
    }

    @Override
    public void run() {
        while (running) {
            if (surfaceHolder.getSurface().isValid()) {
                long currentTime = System.currentTimeMillis();
                long frameTime = currentTime - lastFrameTime; // Tempo entre frames

                // Calcular o FPS (se o frameTime for maior que 0 para evitar divisão por zero)
                if (frameTime > 0) {
                    fps = (int) (1000 / frameTime);
                }

                lastFrameTime = currentTime; // Atualiza o tempo do último frame

                // Bloqueia o canvas e começa a desenhar
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawRGB(255, 255, 255); // Fundo branco

                // Desenha a pista e a linha de largada
                track.draw(canvas);

                // Desenha o carro
                car.draw(canvas);

                // Desenha as informações sobre o carro (tempo, distância, voltas e FPS)
                drawInfo(canvas);

                // Libera o canvas após desenhar
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    // Função para desenhar as informações na tela
    private void drawInfo(Canvas canvas) {
        Paint infoPaint = new Paint();
        infoPaint.setColor(Color.GRAY);
        infoPaint.setTextSize(18);

        // Calcula e desenha as informações
        long currentTime = System.currentTimeMillis();
        int elapsedTime = (int) ((currentTime - startTime) / 1000f); // Tempo decorrido em segundos
        int distanceMoved = car.getTotalDistanceMoved(); // Distância total percorrida pelo carro
        int laps = car.getLapCount(); // Número de voltas

        // Exibe as informações na tela
        canvas.drawText("FPS: " + fps, 10, 30, infoPaint);
        canvas.drawText("Tempo: " + elapsedTime + "s", 10, 50, infoPaint);
        canvas.drawText("Distância: " + distanceMoved + "px", 10, 70, infoPaint);
        canvas.drawText("Voltas: " + laps, 10, 90, infoPaint);
        canvas.drawText("Velocidade: " + car.getSpeed() + "px/s", 10, 110, infoPaint);
    }

    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
        car.start(); // Inicia a thread do carro
    }

    public void pause() {
        running = false;
        car.stopCar(); // Para a thread do carro
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isCollision(float x, float y) {
        // Verifica se o carro está colidindo com algo na pista
        return track.isCollision(x, y);
    }

    public Track getTrack() {
        return track;
    }
}