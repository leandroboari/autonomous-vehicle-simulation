package com.leandroboari.autonomousvehiclesimulation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {
    private List<Car> cars;
    private List<CarPlaceholder> carPlaceholders;
    private List<CarPosition> startingPositions;
    private boolean showPlaceholders = true;
    private Track track;
    private Thread gameThread;
    private SurfaceHolder surfaceHolder;
    private boolean running = false;
    private boolean gameStarted = false;
    private boolean gamePaused = true;
    private long pausedTime = 0;
    private long pauseStartTime = 0;
    private long startTime, lastFrameTime;
    private int fps;
    TextView textViewInfo;

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        surfaceHolder = getHolder();

        startTime = System.currentTimeMillis();
        lastFrameTime = System.currentTimeMillis();

        track = new Track(
                context,
                R.drawable.pista,
                800,
                600,
                684,
                347,
                736,
                402
        );

        startingPositions = new ArrayList<>();
        startingPositions.add(new CarPosition(683, 381, -45));
        startingPositions.add(new CarPosition(700, 399, -45));
        startingPositions.add(new CarPosition(662, 401, -45));
        startingPositions.add(new CarPosition(679, 420, -45));
        startingPositions.add(new CarPosition(635, 422, -45));
        startingPositions.add(new CarPosition(652, 440, -45));
        startingPositions.add(new CarPosition(608, 444, -45));
        startingPositions.add(new CarPosition(623, 464, -45));

        carPlaceholders = new ArrayList<>();
        cars = new ArrayList<>();
    }

    private void createCars(int numCars) {
        for (int i = 0; i < numCars; i++) {
            CarPosition startPos = startingPositions.get(i);

            // Geração de velocidades aleatórias
            Random random = new Random();
            int min = 3;
            int max = 8;
            int minSpeed = random.nextInt(max - min + 1) + min;
            int maxSpeed = random.nextInt(max - min + 1) + min;

            // Criação de carros
            Car car = new Car(
                    this,
                    startPos.getX(),
                    startPos.getY(),
                    startPos.getAngle(),
                    0,
                    minSpeed,
                    maxSpeed,
                    40
            );

            cars.add(car);
            car.start();
        }
    }

    public void createCarPlaceholders(int numCars) {
        carPlaceholders = new ArrayList<>();
        int numPlaceholders = Math.min(numCars, startingPositions.size());

        for (int i = 0; i < numPlaceholders; i++) {
            CarPosition startPos = startingPositions.get(i);
            CarPlaceholder placeholder = new CarPlaceholder(this, startPos.getX(), startPos.getY(), startPos.getAngle());
            carPlaceholders.add(placeholder);
        }
    }

    @Override
    public void run() {
        while (running) {
            if (surfaceHolder.getSurface().isValid()) {
                if (!gamePaused) {
                    long currentTime = System.currentTimeMillis();
                    long frameTime = currentTime - lastFrameTime;
                    fps = (int) (1000 / frameTime);
                    lastFrameTime = currentTime;
                }

                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawRGB(255, 255, 255);

                track.draw(canvas);

                if (showPlaceholders) {
                    for (CarPlaceholder placeholder : carPlaceholders) {
                        placeholder.draw(canvas);
                    }
                }

                for (Car car : cars) {
                    car.draw(canvas);
                }

                drawInfo(canvas);

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private int lastElapsedTime = 0;

    private void drawInfo(Canvas canvas) {
        Paint infoPaint = new Paint();
        infoPaint.setColor(Color.WHITE);
        infoPaint.setTextSize(21);

        long currentTime = System.currentTimeMillis();
        int elapsedTime;

        if (!gamePaused) {
            elapsedTime = (int) ((currentTime - startTime - pausedTime) / 1000f);
            lastElapsedTime = elapsedTime;
        } else {
            elapsedTime = lastElapsedTime;
        }

        int lineSpacing = 28;
        int marginSpacing = 8;
        int baseY = marginSpacing + lineSpacing;

        canvas.drawText("FPS: " + fps, marginSpacing, baseY, infoPaint);
        baseY += lineSpacing;
        canvas.drawText("Tempo: " + elapsedTime + "s", marginSpacing, baseY, infoPaint);

        StringBuilder info = new StringBuilder();
        info.append("Informações dos Carros").append("\n\n");

        for (int i = 0; i < cars.size(); i++) {
            Car car = cars.get(i);
            info.append("Carro ").append(i + 1).append(":\n");
            info.append("Distância: ").append(car.getTotalDistanceMoved()).append("px\n");
            info.append("Voltas: ").append(car.getLapCount()).append("\n");
            info.append("Velocidade: ").append((int) car.getSpeed()).append("px/s\n");
            info.append("Penalidade(s): ").append(car.getPenalties()).append("\n");
            info.append("\n");
        }

        // Atualiza o TextView na thread principal (UI)
        ((MainActivity) getContext()).runOnUiThread(() -> textViewInfo.setText(info.toString()));
    }

    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
        for (Car car : cars) {
            car.start();
        }
    }

    public void pause() {
        running = false;
        for (Car car : cars) {
            car.stopCar();
        }
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void togglePause() {
        gamePaused = !gamePaused;

        if (!gamePaused) {
            if (!gameStarted) {
                startTime = System.currentTimeMillis();
                gameStarted = true;
                createCars(carPlaceholders.size());
                carPlaceholders.clear();
            } else {
                pausedTime += System.currentTimeMillis() - pauseStartTime;
            }
        } else {
            pauseStartTime = System.currentTimeMillis();
        }
    }



    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isGamePaused() {
        return gamePaused;
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

    public void setTextViewInfo(TextView textViewInfo) {
        this.textViewInfo = textViewInfo;
    }
}
