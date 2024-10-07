package com.leandroboari.autonomousvehiclesimulation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Car extends Thread {

    private float x, y, angle, speed;
    private float[] sensorMap = new float[9]; // Distâncias detectadas pelos sensores.
    private float[] sensorAngles = {0, 15, 30, 45, 60, 300, 315, 330, 345};
    private boolean running = true;
    private GameView gameView;
    private Paint carPaint, sensorPaint;
    private PIDController pidController;

    // Contadores e variáveis para distância e voltas
    private float totalDistanceMoved = 0;
    private float previousX, previousY;
    private int lapCount = 0;
    private boolean crossedLine = false; // Controle para verificar se o carro cruzou a linha de chegada

    private final RectF startLine; // Definição da linha de largada para contagem de voltas

    public Car(GameView gameView, float x, float y, float angle, float speed, PIDController pidController) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed;
        this.gameView = gameView;
        this.pidController = pidController;

        this.previousX = x;
        this.previousY = y;

        // Definir a linha de largada com base na pista
        this.startLine = new RectF(573, 404, 627, 478); // Ajuste para a posição da sua linha de largada

        carPaint = new Paint();
        carPaint.setColor(Color.BLUE); // Cor do carro
        sensorPaint = new Paint();
        sensorPaint.setColor(Color.RED); // Cor dos sensores
        sensorPaint.setStrokeWidth(2);
    }

    @Override
    public void run() {
        while (running) {
            moveCar();
            gameView.postInvalidate(); // Atualiza o desenho na tela
            try {
                Thread.sleep(16); // Simula um frame rate de ~60 FPS (não é necessário um FPS controlado rigorosamente).
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
            totalDistanceMoved += Math.sqrt(Math.pow(newX - previousX, 2) + Math.pow(newY - previousY, 2));
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

    private void updateSensors() {
        // Atualiza os sensores verificando a distância para as bordas ou obstáculos.
        for (int i = 0; i < sensorAngles.length; i++) {
            sensorMap[i] = calculateDistanceForSensor(sensorAngles[i]);
        }
    }

    private float calculateDistanceForSensor(float sensorAngle) {
        float maxDistance = 100; // Alcance máximo do sensor
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

    public void draw(Canvas canvas) {
        // Desenha o carro como um retângulo.
        RectF carRect = new RectF(x - 20, y - 10, x + 20, y + 10);
        canvas.save();
        canvas.rotate(angle, x, y);
        canvas.drawRect(carRect, carPaint);
        canvas.restore();

        // Desenha os sensores
        for (int i = 0; i < sensorAngles.length; i++) {
            float sensorAngle = angle + sensorAngles[i];
            float radians = (float) Math.toRadians(sensorAngle);
            float endX = x + sensorMap[i] * (float) Math.cos(radians);
            float endY = y + sensorMap[i] * (float) Math.sin(radians);
            canvas.drawLine(x, y, endX, endY, sensorPaint);
        }
    }

    // Método para verificar se o carro cruzou a linha de chegada
    private void checkLap() {
        if (startLine.contains(x, y)) {
            if (!crossedLine) {
                lapCount++;
                crossedLine = true;
            }
        } else {
            crossedLine = false;
        }
    }

    // Método para obter a distância total percorrida
    public float getTotalDistanceMoved() {
        return totalDistanceMoved;
    }

    // Método para obter o número de voltas
    public int getLapCount() {
        return lapCount;
    }

    // Método para obter a velocidade atual
    public float getSpeed() {
        return speed;
    }
}
