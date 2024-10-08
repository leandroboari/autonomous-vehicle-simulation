package com.leandroboari.autonomousvehiclesimulation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

public class GameView extends SurfaceView implements Runnable {
    private List<Car> cars;
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

        // Inicia os carros
        cars = new ArrayList<>();

        // Carro 1
        cars.add(new Car(
            this,
            683,
            381,
            -20,
            4
        ));

        // Carro 2
        cars.add(new Car(
            this,
            700,
            399,
            -20,
            4
        ));
    }

    @Override
    public void run() {
        while (running) {
            if (surfaceHolder.getSurface().isValid()) {
                long currentTime = System.currentTimeMillis();
                long frameTime = currentTime - lastFrameTime;

                // Calcular o FPS
                if (frameTime > 0) {
                    fps = (int) (1000 / frameTime);
                }

                lastFrameTime = currentTime;

                // Bloqueia o canvas e começa a desenhar
                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawRGB(255, 255, 255); // Fundo branco

                // Desenha a pista e a linha de largada
                track.draw(canvas);

                // Desenha os carros
                for (Car car : cars) {
                    car.draw(canvas);
                }

                // Desenha as informações sobre os carros (tempo, distância, voltas e FPS)
                drawInfo(canvas);

                // Libera o canvas após desenhar
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void drawInfo(Canvas canvas) {
        Paint infoPaint = new Paint();
        infoPaint.setColor(Color.GRAY);
        infoPaint.setTextSize(18);

        long currentTime = System.currentTimeMillis();
        int elapsedTime = (int) ((currentTime - startTime) / 1000f);

        // Variável de controle para posicionar o texto na tela
        int baseY = 30;
        int lineSpacing = 20;

        // Exibir FPS e tempo decorrido
        canvas.drawText("FPS: " + fps, 10, baseY, infoPaint);
        canvas.drawText("Tempo: " + elapsedTime + "s", 10, baseY + lineSpacing, infoPaint);

        baseY = 90;

        // Desenha informações para cada carro
        for (int i = 0; i < cars.size(); i++) {
            Car car = cars.get(i);
            int distanceMoved = car.getTotalDistanceMoved();
            int laps = car.getLapCount();

            // Informações do carro na tela
            canvas.drawText("Carro " + (i + 1) + ":", 10, baseY, infoPaint);
            canvas.drawText("Distância: " + distanceMoved + "px", 10, baseY + lineSpacing, infoPaint);
            canvas.drawText("Voltas: " + laps, 10, baseY + 2 * lineSpacing, infoPaint);
            canvas.drawText("Velocidade: " + car.getSpeed() + "px/s", 10, baseY + 3 * lineSpacing, infoPaint);

            // Ajusta a posição Y para exibir o próximo carro
            baseY += 5 * lineSpacing;
        }


    }

    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
        for (Car car : cars) {
            car.start(); // Inicia a thread de cada carro
        }
    }

    public void pause() {
        running = false;
        for (Car car : cars) {
            car.stopCar(); // Para cada carro
        }
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isCollision(float x, float y) {
        return track.isCollision(x, y);
    }

    public Track getTrack() {
        return track;
    }

    public List<Car> getCars() {
        return cars;
    }
}