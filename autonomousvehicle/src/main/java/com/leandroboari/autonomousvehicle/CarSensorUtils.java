package com.leandroboari.autonomousvehicle;

public class CarSensorUtils {

    // Função modularizada para calcular a nova posição e distância
    public static double[] moveCalc(float x, float y, float speed, float angle, float previousX, float previousY, double totalDistanceMoved) {
        // Converte o ângulo de graus para radianos
        float radians = (float) Math.toRadians(angle);

        // Calcula as novas posições X e Y do carro com base na velocidade e ângulo
        float newX = x + speed * (float) Math.cos(radians);
        float newY = y + speed * (float) Math.sin(radians);

        // Calcula a distância percorrida usando a fórmula da distância euclidiana
        double calcPreviousX = Math.pow(newX - previousX, 2);
        double calcPreviousY = Math.pow(newY - previousY, 2);

        // Atualiza a distância total
        totalDistanceMoved += Math.sqrt(calcPreviousX + calcPreviousY);

        // Retorna um array com newX, newY e totalDistanceMoved
        return new double[]{newX, newY, totalDistanceMoved};
    }

    // Interface funcional para detecção de colisão
    @FunctionalInterface
    public interface Colisao {
        boolean verificar(float x, float y);
    }

    // Função modularizada para calcular a distância detectada por um sensor específico
    public static float calculateDistanceForSensor(
            float sensorAngle,
            float angle,
            float x,
            float y,
            float detectionDistance,
            Colisao isCollision,
            Colisao isCollisionWithOtherCars) {

        float radians = (float) Math.toRadians(angle + sensorAngle); // Converte o ângulo do sensor para radianos
        float dx = (float) Math.cos(radians); // Calcula o deslocamento em X
        float dy = (float) Math.sin(radians); // Calcula o deslocamento em Y
        float distance = 0;

        // Incrementa a distância até atingir a distância de detecção máxima ou uma colisão
        while (distance < detectionDistance) {
            float checkX = x + distance * dx;
            float checkY = y + distance * dy;
            if (isCollision.verificar(checkX, checkY) || isCollisionWithOtherCars.verificar(checkX, checkY)) {
                return distance; // Retorna a distância na qual ocorreu uma colisão
            }
            distance++;
        }
        return detectionDistance; // Retorna a distância máxima se não houve colisão
    }
}
