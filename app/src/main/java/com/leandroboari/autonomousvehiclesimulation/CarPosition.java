package com.leandroboari.autonomousvehiclesimulation;

public class CarPosition {
    private float x;
    private float y;
    private float angle;

    public CarPosition(float x, float y, float angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float rotation) {
        this.angle = rotation;
    }
}