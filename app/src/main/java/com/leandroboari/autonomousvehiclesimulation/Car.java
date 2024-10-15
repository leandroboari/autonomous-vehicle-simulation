package com.leandroboari.autonomousvehiclesimulation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;

public class Car extends Thread {
    // Posições X e Y do carro
    private float x;
    private float y;

    // Ângulo de rotação do carro (em graus)
    private float angle;

    // Velocidade atual do carro
    private float speed;

    // Velocidade mínima e máxima do carro
    private final float minSpeed;
    private final float maxSpeed;

    // Distância máxima de detecção dos sensores
    private final float detectionDistance;

    // Cor do carro
    private final String color;

    // Número de penalidades recebidas pelo carro
    private int penalties;

    // Indica se o carro já recebeu penalidade na posição atual
    private boolean hasPenalty;

    // Array com as distâncias detectadas pelos sensores do carro
    private final float[] sensorMap = new float[9];

    // Ângulos dos sensores em relação ao carro
    private final float[] sensorAngles = {0, 15, 30, 45, 60, 300, 315, 330, 345};

    // Indica se a thread do carro está em execução
    private boolean running = true;

    // Referência ao objeto GameView, onde o carro será desenhado
    private final GameView gameView;

    // Paint utilizado para desenhar os sensores
    private final Paint sensorPaint;

    // Controlador PID para ajustar a direção do carro
    private final PIDController steeringPIDController;

    // Controlador PID para ajustar a velocidade do carro
    private final PIDController speedPIDController;

    // Bitmap da imagem do carro
    private final Bitmap carBitmap;

    // Largura e altura do carro
    private final int carWidth, carHeight;

    // Distância total percorrida pelo carro
    private int totalDistanceMoved = 0;

    // Coordenadas anteriores do carro para calcular a distância percorrida
    private float previousX, previousY;

    // Contador de voltas completadas pelo carro
    private int lapCount = 0;

    // Controle para verificar se o carro cruzou a linha de chegada
    private boolean crossedLine = false;

    // Construtor do carro, inicializa as variáveis
    public Car(
            GameView gameView,
            float x,
            float y,
            float angle,
            float speed,
            float minSpeed,
            float maxSpeed,
            float detectionDistance,
            String color
    ) {
        this.gameView = gameView;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        this.detectionDistance = detectionDistance;
        this.color = color;
        this.previousX = x;
        this.previousY = y;

        // Inicializa o número de penalidades
        this.penalties = 0;

        // O carro não tem penalidade no início
        this.hasPenalty = false;

        // Inicializa o controlador PID para direção
        this.steeringPIDController = new PIDController(0.5f, 0.01f, 0.1f);

        // Inicializa o controlador PID para velocidade
        this.speedPIDController = new PIDController(0.5f, 0.01f, 0.1f);

        // Carrega o bitmap da imagem do carro
        Bitmap originalBitmap = BitmapFactory.decodeResource(
                gameView.getContext().getResources(),
                R.drawable.carro
        );

        // Redimensiona o bitmap do carro para 20x12 pixels
        int newWidth = 20;
        int newHeight = 12;
        carBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false);

        // Define a largura e altura do carro com base no bitmap redimensionado
        carWidth = carBitmap.getWidth();
        carHeight = carBitmap.getHeight();

        // Inicializa o Paint dos sensores com a cor vermelha
        sensorPaint = new Paint();
        sensorPaint.setColor(Color.RED); // Cor dos sensores
        sensorPaint.setStrokeWidth(1); // Espessura das linhas dos sensores
    }

    // Função principal da thread do carro, que move o carro enquanto estiver rodando
    @Override
    public void run() {
        while (running) {
            moveCar(); // Move o carro
            gameView.postInvalidate(); // Solicita a atualização da tela
            try {
                Thread.sleep(16); // Atraso para simular 60 FPS (~16 ms por frame)
            } catch (InterruptedException e) {
                e.printStackTrace(); // Trata exceção de interrupção
            }
        }
    }

    // Função para parar a thread do carro
    public void stopCar() {
        running = false; // Define running como falso para parar a thread
    }

    // Função que move o carro
    private void moveCar() {

        // Não move o carro se o jogo estiver pausado
        if (gameView.isGamePaused()) {
            return;
        }

        // Converte o ângulo de graus para radianos
        float radians = (float) Math.toRadians(angle);

        // Calcula as novas posições X e Y do carro com base na velocidade e ângulo
        float newX = x + speed * (float) Math.cos(radians);
        float newY = y + speed * (float) Math.sin(radians);

        // Calcula a distância percorrida usando a fórmula da distância euclidiana
        double calcPreviousX = Math.pow(newX - previousX, 2);
        double calcPreviousY = Math.pow(newY - previousY, 2);

        // Atualiza a distância total
        totalDistanceMoved += (int) Math.sqrt(calcPreviousX + calcPreviousY);

        // Atualiza a posição X e Y anterior
        previousX = newX;
        previousY = newY;

        // Verifica colisões com a pista ou outros carros
        if (!gameView.isCollision(newX, newY) && !isCollisionWithOtherCars(newX, newY)) {

            // Atualiza a posição X e Y do carro
            x = newX;
            y = newY;

            // Se não houver colisão, ele não é penalizado
            hasPenalty = false;
        } else {
            if (!hasPenalty) {

                // Adiciona uma penalidade se houve colisão
                penalties++;

                // Marca que o carro já recebeu penalidade nessa colisão
                hasPenalty = true;
            }
        }

        // Atualiza os sensores do carro
        updateSensors();

        // Calcula o erro dos sensores laterais
        float error = calculateError();

        // Controlador PID ajusta o ângulo do carro com base no erro dos sensores
        float correction = steeringPIDController.compute(error);

        // Aplica a correção ao ângulo do carro
        angle += correction;

        // Ajusta a velocidade com base na correção da rotação (curvas ou retas)
        adjustSpeedBasedOnRotation(correction);

        // Verifica se o carro cruzou a linha de chegada para contar voltas
        checkLap();
    }

    // Ajusta a velocidade com base na rotação do carro (curvas desaceleram)
    private void adjustSpeedBasedOnRotation(float correction) {
        float deadZone = 2; // Define uma zona morta para a rotação mínima
        float targetSpeed = (Math.abs(correction) > deadZone) ? minSpeed : maxSpeed; // Define a velocidade alvo

        // Calcula o erro de velocidade com base na diferença entre a velocidade atual e a desejada
        float speedError = targetSpeed - speed;

        // Ajusta a velocidade suavemente usando o controlador PID
        float speedAdjustment = speedPIDController.compute(speedError);

        // Aplica o ajuste de velocidade, garantindo que esteja dentro dos limites
        speed = Math.max(minSpeed, Math.min(maxSpeed, speed + speedAdjustment));
    }

    // Verifica se houve colisão com outros carros
    private boolean isCollisionWithOtherCars(float newX, float newY) {
        for (Car otherCar : gameView.getCars()) {
            if (otherCar != this && isPointInCar(newX, newY, otherCar)) {
                return true; // Colisão com outro carro detectada
            }
        }
        return false; // Não houve colisão
    }

    // Verifica se um ponto (x, y) está dentro de outro carro
    private boolean isPointInCar(float x, float y, Car otherCar) {
        float carLeft = otherCar.x - (float) otherCar.getCarWidth() / 2;
        float carRight = otherCar.x + (float) otherCar.getCarWidth() / 2;
        float carTop = otherCar.y - (float) otherCar.getCarHeight() / 2;
        float carBottom = otherCar.y + (float) otherCar.getCarHeight() / 2;

        return x >= carLeft && x <= carRight && y >= carTop && y <= carBottom; // Verifica se o ponto está dentro dos limites do outro carro
    }

    // Atualiza as leituras dos sensores do carro
    private void updateSensors() {
        for (int i = 0; i < sensorAngles.length; i++) {
            sensorMap[i] = calculateDistanceForSensor(sensorAngles[i]); // Calcula a distância para cada sensor
        }
    }

    // Calcula a distância detectada por um sensor específico
    private float calculateDistanceForSensor(float sensorAngle) {
        float radians = (float) Math.toRadians(angle + sensorAngle); // Converte o ângulo do sensor para radianos
        float dx = (float) Math.cos(radians); // Calcula o deslocamento em X
        float dy = (float) Math.sin(radians); // Calcula o deslocamento em Y
        float distance = 0;

        // Incrementa a distância até atingir a distância de detecção máxima ou uma colisão
        while (distance < detectionDistance) {
            float checkX = x + distance * dx;
            float checkY = y + distance * dy;
            if (gameView.isCollision(checkX, checkY) || isCollisionWithOtherCars(checkX, checkY)) {
                return distance; // Retorna a distância na qual ocorreu uma colisão
            }
            distance++;
        }
        return detectionDistance; // Retorna a distância máxima se não houve colisão
    }

    // Calcula o erro entre os sensores laterais para ajustar a direção do carro
    private float calculateError() {
        float leftAvg = (sensorMap[5] + sensorMap[6] + sensorMap[7]) / 3; // Média dos sensores do lado esquerdo
        float rightAvg = (sensorMap[2] + sensorMap[3] + sensorMap[4]) / 3; // Média dos sensores do lado direito

        if (leftAvg >= detectionDistance && rightAvg >= detectionDistance) {
            return 0; // Sem obstáculos detectados, sem erro
        }

        return rightAvg - leftAvg; // Retorna a diferença entre os sensores laterais
    }

    // Verifica se o carro completou uma volta ao cruzar a linha de chegada
    private void checkLap() {
        if (totalDistanceMoved > 200) { // Somente verifica se o carro já percorreu uma certa distância
            RectF startLine = new RectF(gameView.getTrack().getStartLineRect()); // Linha de chegada
            if (startLine.contains(x, y)) {
                if (!crossedLine) {
                    lapCount++; // Incrementa o contador de voltas
                    crossedLine = true; // Marca que o carro cruzou a linha
                }
            } else {
                crossedLine = false; // Redefine o estado da linha de chegada
            }
        }
    }

    // Desenha o carro e seus sensores no canvas
    public void draw(Canvas canvas) {
        canvas.save(); // Salva o estado do canvas

        // Rotaciona o canvas com base no ângulo do carro
        canvas.rotate(angle, x, y);

        // Cria um Paint para o carro com um filtro de cor
        Paint carPaint = new Paint();
        carPaint.setColorFilter(
                new PorterDuffColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP)
        );

        // Desenha o bitmap do carro no canvas, centralizando na posição (x, y)
        canvas.drawBitmap(carBitmap, x - (float) carWidth / 2, y - (float) carHeight / 2, carPaint);

        canvas.restore(); // Restaura o estado do canvas

        // Desenha os sensores do carro
        for (int i = 0; i < sensorAngles.length; i++) {
            float sensorAngle = angle + sensorAngles[i]; // Ângulo do sensor em relação ao carro
            float radians = (float) Math.toRadians(sensorAngle);
            float endX = x + sensorMap[i] * (float) Math.cos(radians); // Calcula a posição final X do sensor
            float endY = y + sensorMap[i] * (float) Math.sin(radians); // Calcula a posição final Y do sensor

            // Desenha a linha do sensor no canvas
            canvas.drawLine(x, y, endX, endY, sensorPaint);
        }
    }

    // Métodos getters para obter as informações do carro

    public int getTotalDistanceMoved() {
        return totalDistanceMoved;
    }

    public int getLapCount() {
        return lapCount;
    }

    public float getSpeed() {
        return speed;
    }

    public int getCarWidth() {
        return carWidth;
    }

    public int getCarHeight() {
        return carHeight;
    }

    public int getPenalties() {
        return penalties;
    }
}
