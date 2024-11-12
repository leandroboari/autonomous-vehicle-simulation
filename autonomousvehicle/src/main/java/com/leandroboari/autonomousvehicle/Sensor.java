package com.leandroboari.autonomousvehicle;

public class Sensor {

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
}
