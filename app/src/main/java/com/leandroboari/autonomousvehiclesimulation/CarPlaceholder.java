package com.leandroboari.autonomousvehiclesimulation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

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
        carBitmap = Bitmap.createScaledBitmap(originalBitmap, 10, 6, false);

        carWidth = carBitmap.getWidth();
        carHeight = carBitmap.getHeight();
    }

    public void draw(Canvas canvas) {
        // Salva o estado do canvas para futuras restaurações
        canvas.save();

        // Rotaciona o carro baseado no ângulo
        canvas.rotate(angle, x, y);

        // Cria um objeto Paint para o carro com uma cor definida
        Paint carPaint = new Paint();
        carPaint.setColorFilter(
                new PorterDuffColorFilter(
                        Color.parseColor("#888888"),
                        PorterDuff.Mode.SRC_ATOP
                )
        );

        // Desenha o bitmap do carro no centro do carro
        canvas.drawBitmap(
                carBitmap,
                x - (float) carWidth / 2,
                y - (float) carHeight / 2,
                carPaint // Aplicando a cor ao carro
        );

        // Restaura o estado do canvas
        canvas.restore();
    }
}
