package com.leandroboari.autonomousvehiclesimulation;

public class CarPosition {
    private float x;
    private float y;
    private float angle;
    private String color;
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

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setAngle(float angle) { this.angle = angle; }
    public void setColor(String color) { this.color = color; }
}