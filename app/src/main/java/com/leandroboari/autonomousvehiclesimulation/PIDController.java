package com.leandroboari.autonomousvehiclesimulation;

public class PIDController {
    private float kp, ki, kd;
    private float previousError = 0;
    private float integral = 0;

    public PIDController(float kp, float ki, float kd) {
        this.kp = kp;
        this.ki = ki;
        this.kd = kd;
    }

    public float compute(float error) {
        integral += error;
        float derivative = error - previousError;
        float output = kp * error + ki * integral + kd * derivative;
        previousError = error;
        return output;
    }
}
