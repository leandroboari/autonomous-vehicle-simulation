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
    private float speed;
    private final float minSpeed;
    private final float maxSpeed;
    private final float detectionDistance;
//    private int fuelTank;
//    private int penalty;
    private final float[] sensorMap = new float[9]; // Distâncias detectadas pelos sensores.
    private final float[] sensorAngles = {0, 15, 30, 45, 60, 300, 315, 330, 345};
    private boolean running = true;
    private final GameView gameView;
    private final Paint sensorPaint;
    private final PIDController pidController;
    private final PIDController speedPIDController;

    private final Bitmap carBitmap; // Bitmap para a imagem do carro
    private final int carWidth, carHeight; // Dimensões do carro

    // Contadores e variáveis para distância e voltas
    private int totalDistanceMoved = 0;
    private float previousX, previousY;
    private int lapCount = 0;

    // Controle para verificar se o carro cruzou a linha de chegada
    private boolean crossedLine = false;

    public Car(
            GameView gameView,
            float x,
            float y,
            float angle,
            float speed,
            float minSpeed,
            float maxSpeed,
            float detectionDistance
    ) {
        this.gameView = gameView;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed; // Velocidade em pixels por frame
        this.minSpeed = minSpeed;  // Velocidade mínima definida pelo usuário
        this.maxSpeed = maxSpeed;  // Velocidade máxima definida pelo usuário
        this.detectionDistance = detectionDistance;
        this.previousX = x;
        this.previousY = y;

        // Controlador PID para direção
        this.pidController = new PIDController(0.5f, 0.01f, 0.1f);

        // Controlador PID para velocidade
        this.speedPIDController = new PIDController(0.5f, 0.01f, 0.1f);

        // Carrega o bitmap da imagem do carro
        Bitmap originalBitmap = BitmapFactory.decodeResource(
                gameView.getContext().getResources(),
                R.drawable.carro
        );

        // Redimensiona o carro para um tamanho menor, por exemplo, 50% do tamanho original
        int newWidth = 20;  // Reduz para 50% da largura
        int newHeight = 12; // Reduz para 50% da altura
        carBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false);

        // Define a largura e altura do carro com base no bitmap redimensionado
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
                //noinspection BusyWait
                Thread.sleep(16); // Simula um frame rate de ~60 FPS
            } catch (InterruptedException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }
    }

    public void stopCar() {
        running = false;
    }

    private void moveCar() {

        if (gameView.isGamePaused()) {
            return;
        }

        // Atualiza a posição do carro com base na velocidade e ângulo.
        float radians = (float) Math.toRadians(angle);
        float newX = x + speed * (float) Math.cos(radians);
        float newY = y + speed * (float) Math.sin(radians);

        // Atualiza a distância total percorrida
        double calcPreviousX = Math.pow(newX - previousX, 2);
        double calcPreviousY = Math.pow(newY - previousY, 2);
        totalDistanceMoved += (int) Math.sqrt(calcPreviousX + calcPreviousY);
        previousX = newX;
        previousY = newY;

        // Verifica colisão com a pista e outros carros
        if (!gameView.isCollision(newX, newY) && !isCollisionWithOtherCars(newX, newY)) {
            x = newX;
            y = newY;
        }

        // Atualiza os sensores
        updateSensors();

        // Calcula o erro baseado nos sensores laterais
        float error = calculateError();

        // Controle PID para ajustar o ângulo do carro com base no erro
        float correction = pidController.compute(error);
        angle += correction; // Ajuste do ângulo

        // Ajusta a velocidade com base na rotação e frenagem nas curvas
        adjustSpeedBasedOnRotation(correction);

        // Verifica se o carro cruzou a linha de largada/chegada
        checkLap();
    }

    private void adjustSpeedBasedOnRotation(float correction) {
        // Define a zona morta para a rotação ser insignificante (reta)
        float deadZone = 2;

        // Se a rotação for maior que a zona morta, desacelere (curva), caso contrário, acelere
        float targetSpeed;
        if (Math.abs(correction) > deadZone) {
            targetSpeed = minSpeed; // Velocidade mínima nas curvas
        } else {
            targetSpeed = maxSpeed; // Velocidade máxima nas retas
        }

        // Calcula o erro de velocidade com base na diferença entre a velocidade atual e a desejada
        float speedError = targetSpeed - speed;

        // Usa o controlador PID para ajustar a velocidade suavemente
        float speedAdjustment = speedPIDController.compute(speedError);

        // Aplica a mudança de velocidade de forma gradual, respeitando os limites
        speed = Math.max(minSpeed, Math.min(maxSpeed, speed + speedAdjustment));
    }

    private boolean isCollisionWithOtherCars(float newX, float newY) {
        for (Car otherCar : gameView.getCars()) {
            if (otherCar != this && isPointInCar(newX, newY, otherCar)) {
                return true; // Colisão com outro carro detectada
            }
        }
        return false;
    }

    private boolean isPointInCar(float x, float y, Car otherCar) {
        // Verifica se o ponto (x, y) está dentro do outro carro
        float carLeft = otherCar.x - (float) otherCar.getCarWidth() / 2;
        float carRight = otherCar.x + (float) otherCar.getCarWidth() / 2;
        float carTop = otherCar.y - (float) otherCar.getCarHeight() / 2;
        float carBottom = otherCar.y + (float) otherCar.getCarHeight() / 2;

        return x >= carLeft && x <= carRight && y >= carTop && y <= carBottom;
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
        float radians = (float) Math.toRadians(angle + sensorAngle);
        float dx = (float) Math.cos(radians);
        float dy = (float) Math.sin(radians);
        float distance = 0;

        while (distance < detectionDistance) {
            float checkX = x + distance * dx;
            float checkY = y + distance * dy;

            if (gameView.isCollision(checkX, checkY) || isCollisionWithOtherCars(checkX, checkY)) {
                return distance; // Colisão detectada
            }

            distance++;
        }
        return detectionDistance; // Sem obstáculos detectados dentro da distância máxima
    }

    private float calculateError() {
        // Sensores laterais esquerdos (330, 315, 300) e direitos (30, 45, 60)
        float leftAvg = (sensorMap[5] + sensorMap[6] + sensorMap[7]) / 3;
        float rightAvg = (sensorMap[2] + sensorMap[3] + sensorMap[4]) / 3;

        // Se os sensores não estão detectando nada próximo, não há erro a corrigir
        if (leftAvg >= detectionDistance && rightAvg >= detectionDistance) {
            return 0; // Se todos os sensores estão "limpos", não há erro
        }

        // Retorna a diferença entre sensores laterais para calcular o erro
        return rightAvg - leftAvg;
    }


    private void checkLap() {

        // Não verifica a linha de chegada na largada
        if(totalDistanceMoved > 200) {

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

    public int getCarWidth() { return carWidth; }

    public int getCarHeight() { return carHeight; }
}