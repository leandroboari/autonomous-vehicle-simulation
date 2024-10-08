package com.leandroboari.autonomousvehiclesimulation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Car extends Thread {

    private float x;
    private float y;
    private float angle;
    private final float speed;
    private int fuelTank;
    private int penalty;
    private final float[] sensorMap = new float[9]; // Distâncias detectadas pelos sensores.
    private final float[] sensorAngles = {0, 15, 30, 45, 60, 300, 315, 330, 345};
    private boolean running = true;
    private final GameView gameView;
    private final Paint sensorPaint;
    private final PIDController pidController;

    private final Bitmap carBitmap; // Bitmap para a imagem do carro
    private final int carWidth, carHeight; // Dimensões do carro

    // Contadores e variáveis para distância e voltas
    private int totalDistanceMoved = 0;
    private float previousX, previousY;
    private int lapCount = 0;
    private boolean crossedLine = false; // Controle para verificar se o carro cruzou a linha de chegada

    public Car(GameView gameView, float x, float y, float angle, float speed) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed; // Velocidade em pixels por frame
        this.gameView = gameView;
        this.previousX = x;
        this.previousY = y;

        // Definir PID controller
        this.pidController = new PIDController(0.8f, 0.01f, 0.1f);

        // Carrega o bitmap da imagem do carro
        carBitmap = BitmapFactory.decodeResource(gameView.getContext().getResources(), R.drawable.carro);

        // Define a largura e altura do carro
        carWidth = carBitmap.getWidth();
        carHeight = carBitmap.getHeight();

        sensorPaint = new Paint();
        sensorPaint.setColor(Color.RED); // Cor dos sensores
        sensorPaint.setStrokeWidth(1);
    }

    @Override
    public void run() {
        while (running) {
            moveCar();
            gameView.postInvalidate(); // Atualiza o desenho na tela
            try {
                Thread.sleep(16); // Simula um frame rate de ~60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopCar() {
        running = false;
    }

    private void moveCar() {
        // Atualiza a posição do carro com base na velocidade e ângulo.
        float radians = (float) Math.toRadians(angle);
        float newX = x + speed * (float) Math.cos(radians);
        float newY = y + speed * (float) Math.sin(radians);

        // Só atualiza a posição se não houver colisão
        if (!gameView.isCollision(newX, newY)) {

            // Atualiza a distância total percorrida
            double calcPreviousX = Math.pow(newX - previousX, 2);
            double calcPreviousY = Math.pow(newY - previousY, 2);
            totalDistanceMoved += (int) Math.sqrt(calcPreviousX + calcPreviousY);
            previousX = newX;
            previousY = newY;

            x = newX;
            y = newY;
        }

        // Corrigir rota com o controlador PID
        updateSensors();
        float error = calculateError();
        float correction = pidController.compute(error);
        angle += correction; // Ajuste do ângulo

        // Verifica se o carro cruzou a linha de largada/chegada
        checkLap();
    }

    public void draw(Canvas canvas) {
        // Desenha o carro usando a imagem (Bitmap)
        canvas.save();
        canvas.rotate(angle, x, y); // Rotaciona o carro baseado no ângulo

        // Desenha o bitmap do carro no centro do carro (ajustado para que o centro do bitmap seja
        // o ponto de rotação)
        canvas.drawBitmap(
                carBitmap,
                x - (float) carWidth / 2,
                y - (float) carHeight / 2,
                null
        );

        canvas.restore();

        // Desenha os sensores em vermelho
        for (int i = 0; i < sensorAngles.length; i++) {
            float sensorAngle = angle + sensorAngles[i]; // Ângulo do sensor em relação ao carro
            float radians = (float) Math.toRadians(sensorAngle);
            float endX = x + sensorMap[i] * (float) Math.cos(radians); // Posição final X do sensor
            float endY = y + sensorMap[i] * (float) Math.sin(radians); // Posição final Y do sensor

            // Desenha a linha do sensor
            canvas.drawLine(x, y, endX, endY, sensorPaint);
        }
    }

    private void updateSensors() {
        // Atualiza os sensores verificando a distância para as bordas ou obstáculos.
        for (int i = 0; i < sensorAngles.length; i++) {
            sensorMap[i] = calculateDistanceForSensor(sensorAngles[i]);
        }
    }

    private float calculateDistanceForSensor(float sensorAngle) {
        float maxDistance = 50; // Alcance máximo do sensor
        float radians = (float) Math.toRadians(angle + sensorAngle);
        float dx = (float) Math.cos(radians);
        float dy = (float) Math.sin(radians);
        float distance = 0;

        while (distance < maxDistance) {
            float checkX = x + distance * dx;
            float checkY = y + distance * dy;

            if (gameView.isCollision(checkX, checkY)) {
                return distance;
            }

            distance++;
        }
        return maxDistance;
    }

    private float calculateError() {
        // Erro é a diferença entre sensores à direita e esquerda.
        float leftAvg = (sensorMap[5] + sensorMap[6] + sensorMap[7]) / 3;
        float rightAvg = (sensorMap[2] + sensorMap[3] + sensorMap[4]) / 3;
        return rightAvg - leftAvg;
    }

    private void checkLap() {
        RectF startLine = new RectF(gameView.getTrack().getStartLineRect());
        if (startLine.contains(x, y)) {
            if (!crossedLine) {
                lapCount++;
                crossedLine = true;
            }
        } else {
            crossedLine = false;
        }
    }

    public int getTotalDistanceMoved() {
        return totalDistanceMoved;
    }

    public int getLapCount() {
        return lapCount;
    }

    public float getSpeed() {
        return speed;
    }
}