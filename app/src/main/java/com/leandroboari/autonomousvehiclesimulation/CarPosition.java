package com.leandroboari.autonomousvehiclesimulation;

public class CarPosition {
    private final float x;
    private final float y;
    private final float angle;
    private final String color;
    public CarPosition(float x, float y, float angle, String color) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.color = color;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public float getAngle() {
        return angle;
    }
    public String getColor() {
        return color;
    }
}