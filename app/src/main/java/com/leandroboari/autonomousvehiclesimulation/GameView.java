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

import com.leandroboari.autonomousvehicle.Database;

public class GameView extends SurfaceView implements Runnable {

    // Lista de carros e placeholders
    private List<Car> cars;
    private List<CarPlaceholder> carPlaceholders;

    // Posições de partida dos carros
    private List<CarPosition> startingPositions;

    // Indicador para exibir placeholders
    private boolean showPlaceholders = true;

    // Objeto da pista
    private Track track;

    // Thread principal do jogo
    private Thread gameThread;

    // Gerenciamento da superfície de desenho
    private SurfaceHolder surfaceHolder;

    // Controle de execução do jogo
    private boolean running = false;
    private boolean gameStarted = false;
    private boolean gamePaused = true;

    // Controle de tempo de pausa
    private long pausedTime = 0;
    private long pauseStartTime = 0;
    private long startTime, lastFrameTime;
    private int lastElapsedTime = 0;

    // Frames por segundo
    private int fps;

    // Exibição de informações na interface
    private TextView textViewInfo;

    // Construtores para inicializar o GameView
    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    // Inicialização do GameView
    public void init(Context context) {
        surfaceHolder = getHolder();

        // Recupera dados do banco de dados
        Database.getLastCarData();

        // Inicialização do tempo
        startTime = System.currentTimeMillis();
        lastFrameTime = System.currentTimeMillis();

        // Inicialização da pista
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

        // Configuração das posições iniciais dos carros
        setupStartingPositions();

        // Inicialização das listas
        carPlaceholders = new ArrayList<>();
        cars = new ArrayList<>();
    }

    // Define as posições iniciais dos carros
    private void setupStartingPositions() {
        startingPositions = new ArrayList<>();
        startingPositions.add(new CarPosition(683, 381, -45, "#00DA62"));
        startingPositions.add(new CarPosition(700, 399, -45, "#0066DA"));
        startingPositions.add(new CarPosition(662, 401, -45, "#5B00DA"));
        startingPositions.add(new CarPosition(679, 420, -45, "#DA00BD"));
        startingPositions.add(new CarPosition(635, 422, -45, "#DA0004"));
        startingPositions.add(new CarPosition(652, 440, -45, "#DA9500"));
        startingPositions.add(new CarPosition(608, 444, -45, "#FFA600"));
        startingPositions.add(new CarPosition(623, 464, -45, "#A0DA00"));
    }

    // Cria placeholders para os carros
    public void createCarPlaceholders(int numCars) {
        carPlaceholders.clear(); // Evita duplicação de placeholders
        int numPlaceholders = Math.min(numCars, startingPositions.size());

        for (int i = 0; i < numPlaceholders; i++) {
            CarPosition startPos = startingPositions.get(i);
            CarPlaceholder placeholder = new CarPlaceholder(
                    this,
                    startPos.getX(),
                    startPos.getY(),
                    startPos.getAngle()
            );
            carPlaceholders.add(placeholder);
        }
    }

    // Cria os carros para o jogo
    private void createCars(int numCars) {
        try {
            for (int i = 0; i < numCars; i++) {
                CarPosition position = startingPositions.get(i);
                Random random = new Random();
                int maxSpeed = random.nextInt(5) + 4;
                int minSpeed = maxSpeed - 3;
                Car car = new Car(this,
                        position.getX(),
                        position.getY(),
                        position.getAngle(),
                        0,
                        minSpeed,
                        maxSpeed,
                        40,
                        position.getColor()
                );
                cars.add(car);
                car.start();
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace(); // Verifica se as posições estão corretas
        } catch (Exception e) {
            e.printStackTrace(); // Log de qualquer outra exceção ao criar carros
        }
    }

    // Thread de execução do jogo
    @Override
    public void run() {
        while (running) {
            if (surfaceHolder.getSurface().isValid()) {
                try {
                    if (!gamePaused) {
                        updateFPS();
                    }

                    Canvas canvas = surfaceHolder.lockCanvas();
                    if (canvas != null) {
                        canvas.drawRGB(255, 255, 255); // Fundo branco
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
                        surfaceHolder.unlockCanvasAndPost(canvas); // Finaliza o desenho
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // Log da exceção de desenho
                }
            }
        }
    }

    // Atualiza a taxa de frames por segundo
    private void updateFPS() {
        long currentTime = System.currentTimeMillis();
        long frameTime = currentTime - lastFrameTime;
        fps = (int) (1000 / frameTime);
        lastFrameTime = currentTime;
    }

    // Desenha as informações dos carros e o FPS
    private void drawInfo(Canvas canvas) {
        Paint infoPaint = new Paint();
        infoPaint.setColor(Color.WHITE); // Cor do texto
        infoPaint.setTextSize(21); // Tamanho do texto

        // Calcula o tempo de jogo
        int elapsedTime = calculateElapsedTime();

        // Exibe o FPS e o tempo no canvas
        int lineSpacing = 28;
        int marginSpacing = 8;
        int baseY = marginSpacing + lineSpacing;
        canvas.drawText("FPS: " + fps, marginSpacing, baseY, infoPaint);
        baseY += lineSpacing;
        canvas.drawText("Tempo: " + elapsedTime + "s", marginSpacing, baseY, infoPaint);

        // Atualiza as informações dos carros
        updateCarInfo();
    }

    // Calcula o tempo decorrido desde o início
    private int calculateElapsedTime() {
        long currentTime = System.currentTimeMillis();
        if (!gamePaused) {
            lastElapsedTime = (int) ((currentTime - startTime - pausedTime) / 1000f);
        }
        return lastElapsedTime;
    }

    // Atualiza as informações dos carros e as exibe no TextView
    private void updateCarInfo() {
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

        // Atualiza o TextView na UI principal
        ((MainActivity) getContext()).runOnUiThread(() -> textViewInfo.setText(info.toString()));
    }

    // Inicia a execução do jogo
    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();

        // Retoma a execução dos carros
        for (Car car : cars) {
            car.start();
        }
    }

    // Pausa a execução do jogo
    public void pause() {
        running = false;
        try {
            for (Car car : cars) {
                car.stopCar();
            }
            if (gameThread != null) {
                gameThread.join(); // Aguarda a thread principal terminar
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace(); // Log da exceção
        } catch (Exception e) {
            e.printStackTrace(); // Tratamento de exceção genérica
        }
    }

    // Alterna entre pausa e execução
    public void togglePause() {
        try {
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
        } catch (Exception e) {
            e.printStackTrace(); // Log de erro no controle de pausa
        }
    }

    // Verifica se o jogo já começou
    public boolean isGameStarted() {
        return gameStarted;
    }

    // Verifica se o jogo está pausado
    public boolean isGamePaused() {
        return gamePaused;
    }

    // Verifica se há colisão em determinada coordenada (x, y)
    public boolean isCollision(float x, float y) {
        return track.isCollision(x, y); // Usa a função de colisão da pista
    }

    public void endGame() {
        for (Car car : cars) {
            double x = car.getX();
            double y = car.getY();
            double angle = car.getAngle();
            String color = car.getColor();
            double maxSpeed = car.getMaxSpeed();
            double minSpeed = car.getMinSpeed();
            double penalties = car.getPenalties();
            double speed = car.getSpeed();
            Database.saveCarDataToFirestore(x, y, angle, color, maxSpeed, minSpeed, penalties, speed);
        }


        // Pausa o jogo antes de finalizar
        if(!this.isGamePaused()) {
            this.togglePause();
        }
    }

    // Retorna a pista associada a este GameView
    public Track getTrack() {
        return track;
    }

    // Retorna a lista de carros
    public List<Car> getCars() {
        return cars;
    }

    // Define o TextView para exibição de informações
    public void setTextViewInfo(TextView textViewInfo) {
        this.textViewInfo = textViewInfo;
    }
}