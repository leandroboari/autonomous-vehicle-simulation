package com.leandroboari.autonomousvehiclesimulation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class CarPlaceholder {
    private float x;
    private float y;
    private float angle;
    private Bitmap carBitmap; // Bitmap para a imagem do carro
    private final int carWidth, carHeight; // Dimensões do carro

    public CarPlaceholder(GameView gameView, float x, float y, float angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;

        // Carrega o bitmap da imagem do carro
        Bitmap originalBitmap = BitmapFactory.decodeResource(
                gameView.getContext().getResources(),
                R.drawable.carro
        );

        // Redimensiona o carro
        carBitmap = Bitmap.createScaledBitmap(originalBitmap, 20, 12, false);

        carWidth = carBitmap.getWidth();
        carHeight = carBitmap.getHeight();
    }

    public void draw(Canvas canvas) {
        // Desenha o carro usando a imagem (Bitmap)
        canvas.save();
        canvas.rotate(angle, x, y); // Rotaciona o carro baseado no ângulo
        canvas.drawBitmap(
                carBitmap,
                x - (float) carWidth / 2,
                y - (float) carHeight / 2,
                null
        );
        canvas.restore();
    }
}
