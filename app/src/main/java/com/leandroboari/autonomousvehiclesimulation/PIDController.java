package com.leandroboari.autonomousvehiclesimulation;

public class PIDController {

    // Parâmetros do controlador PID
    private float kp;  // Constante proporcional
    private float ki;  // Constante integral
    private float kd;  // Constante derivativa

    // Armazena o erro anterior, necessário para o cálculo da derivada
    private float previousError = 0;

    // Acumula o erro ao longo do tempo para o cálculo da integral
    private float integral = 0;

    // Construtor do PIDController
    // Recebe os parâmetros kp, ki, kd para configurar o controlador PID
    public PIDController(float kp, float ki, float kd) {
        this.kp = kp;  // Define o valor da constante proporcional
        this.ki = ki;  // Define o valor da constante integral
        this.kd = kd;  // Define o valor da constante derivativa
    }

    // Método que calcula a saída do controlador PID com base no erro atual
    public float compute(float error) {
        // Acumula o erro ao longo do tempo (usado para o termo integral)
        integral += error;

        // Calcula a derivada do erro (diferença entre o erro atual e o erro anterior)
        float derivative = error - previousError;

        // Calcula a saída do controlador PID:
        // - Termo proporcional: kp * erro
        // - Termo integral: ki * integral (soma acumulada dos erros)
        // - Termo derivativo: kd * derivada (taxa de mudança do erro)
        float output = kp * error + ki * integral + kd * derivative;

        // Atualiza o erro anterior com o valor do erro atual, para o cálculo no próximo ciclo
        previousError = error;

        // Retorna o valor de saída do controlador PID, que será usado para ajustar o sistema
        return output;
    }
}
