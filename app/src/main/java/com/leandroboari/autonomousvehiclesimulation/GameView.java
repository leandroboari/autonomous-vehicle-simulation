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

        // Inicializa o tempo de início
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

        // Inicia os carros
        cars = new ArrayList<>();

        // Carro 1
//        cars.add(new Car(
//                this,
//                683,
//                381,
//                -45,
//                4,
//                1,
//                8,
//                50
//        ));

    }

    public void createCarPlaceholders(int numCars) {
        carPlaceholders = new ArrayList<>();  // Inicializa a lista de placeholders

        // Limita o número de carros ao tamanho da lista de startingPositions
        int numPlaceholders = Math.min(numCars, startingPositions.size());

        for (int i = 0; i < numPlaceholders; i++) {
            CarPosition startPos = startingPositions.get(i);
            CarPlaceholder placeholder = new CarPlaceholder(this, startPos.getX(), startPos.getY(), startPos.getAngle());
            carPlaceholders.add(placeholder);  // Adiciona à lista de placeholders
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
                canvas.drawRGB(255, 255, 255); // Fundo branco

                // Desenha a pista
                track.draw(canvas);

                // Desenha os placeholders (se habilitado)
                if (showPlaceholders) {
                    for (CarPlaceholder placeholder : carPlaceholders) {
                        placeholder.draw(canvas);
                    }
                }

                // Desenha os carros
                for (Car car : cars) {
                    car.draw(canvas);
                }

                drawInfo(canvas);

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }


    private int lastElapsedTime = 0;  // Para armazenar o tempo da última atualização válida

    private void drawInfo(Canvas canvas) {
        Paint infoPaint = new Paint();
        infoPaint.setColor(Color.WHITE);
        infoPaint.setTextSize(21);

        long currentTime = System.currentTimeMillis();
        int elapsedTime;

        if (!gamePaused) {
            // Atualiza o tempo apenas se o jogo não estiver pausado
            elapsedTime = (int) ((currentTime - startTime - pausedTime) / 1000f);
            lastElapsedTime = elapsedTime;  // Armazena o tempo calculado
        } else {
            // Se estiver pausado, mantém o último valor de tempo calculado
            elapsedTime = lastElapsedTime;
        }

        // Variável de controle para posicionar o texto na tela
        int lineSpacing = 28;
        int marginSpacing = 8;
        int baseY = marginSpacing + lineSpacing;

        // Exibir FPS e tempo decorrido
        canvas.drawText("FPS: " + fps, marginSpacing, baseY, infoPaint);

        baseY += lineSpacing;
        canvas.drawText("Tempo: " + elapsedTime + "s", marginSpacing, baseY, infoPaint);

        baseY = baseY + lineSpacing + marginSpacing;

//        // Desenha informações para cada carro
//        for (int i = 0; i < cars.size(); i++) {
//            Car car = cars.get(i);
//            int distanceMoved = car.getTotalDistanceMoved();
//            int laps = car.getLapCount();
//
//            int velocidade = (int) car.getSpeed();
//
//            // Informações do carro na tela
//            canvas.drawText("Carro " + (i + 1) + ":", marginSpacing, baseY, infoPaint);
//            canvas.drawText("Distância: " + distanceMoved + "px", marginSpacing, baseY + lineSpacing, infoPaint);
//            canvas.drawText("Voltas: " + laps, marginSpacing, baseY + 2 * lineSpacing, infoPaint);
//            canvas.drawText("Velocidade: " + velocidade + "px/s", marginSpacing, baseY + 3 * lineSpacing, infoPaint);
//
//            // Ajusta a posição Y para exibir o próximo carro
//            baseY += (4 * lineSpacing) + marginSpacing;
//        }
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
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public void togglePause() {
        gamePaused = !gamePaused;

        if (!gamePaused) {
            // Se o jogo ainda não começou, define o startTime e cria os carros
            if (!gameStarted) {
                startTime = System.currentTimeMillis();
                gameStarted = true;

                // Cria os carros reais apenas quando o jogo começar
                createCars(carPlaceholders.size());

                // Remove os placeholders
                carPlaceholders.clear();
            } else {
                // Caso contrário, calcula o tempo total que o jogo ficou pausado
                pausedTime += System.currentTimeMillis() - pauseStartTime;
            }
        } else {
            // Registra o momento em que o jogo foi pausado
            pauseStartTime = System.currentTimeMillis();
        }
    }


    private void createCars(int numCars) {
        // Para cada posição inicial disponível, cria um novo carro
        for (int i = 0; i < numCars; i++) {
            CarPosition startPos = startingPositions.get(i);

            // Cria uma nova instância de Carro
            Car car = new Car(
                    this,
                    startPos.getX(),
                    startPos.getY(),
                    startPos.getAngle(),
                    4,   // Velocidade inicial do carro (exemplo)
                    1,   // Direção inicial (exemplo)
                    8,   // Aceleração (exemplo)
                    50   // Velocidade máxima (exemplo)
            );

            // Adiciona o carro à lista de carros
            cars.add(car);

            car.start();
        }
    }

    public boolean isGameStarted() { return gameStarted; }

    public boolean isGamePaused() { return gamePaused; }

    public boolean isCollision(float x, float y) {
        return track.isCollision(x, y);
    }

    public Track getTrack() { return track; }

    public List<Car> getCars() { return cars; }
}