package com.leandroboari.autonomousvehiclesimulation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.core.content.ContextCompat;

public class Track {
    private final Bitmap trackBitmap;
    private final Rect startLine; // A linha de partida/chegada da pista
    private final Paint linePaint;
    private final int trackWidth, trackHeight; // Largura e altura desejadas da pista em pixels

    public Track(Context context, int trackResourceId, int trackWidth, int trackHeight, int startLineX1, int startLineY1, int startLineX2, int startLineY2) {
        // Define a largura e altura da pista em pixels reais
        this.trackWidth = trackWidth;
        this.trackHeight = trackHeight;

        // Carrega a imagem da pista a partir de recursos.
        Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), trackResourceId);

        // Redimensiona o bitmap para o tamanho especificado (trackWidth x trackHeight)
        trackBitmap = Bitmap.createScaledBitmap(originalBitmap, trackWidth, trackHeight, false);

        // Define as coordenadas da linha de largada/chegada com pixels reais (sem redimensionamento)
        startLine = new Rect(startLineX1, startLineY1, startLineX2, startLineY2);

        // Inicializa a pintura da linha de largada/chegada.
        linePaint = new Paint();
        linePaint.setColor(ContextCompat.getColor(context, android.R.color.holo_green_dark)); // Cor da linha verde.
        linePaint.setStrokeWidth(5); // Espessura da linha.
    }

    public void draw(Canvas canvas) {
        // Desenha a pista redimensionada para o tamanho especificado
        canvas.drawBitmap(trackBitmap, 0, 0, null);

        // Desenha a linha de largada/chegada com pixels reais
        canvas.drawLine(startLine.left, startLine.top, startLine.right, startLine.bottom, linePaint);
    }

    public boolean isCollision(float x, float y) {
        // Verifica se as coordenadas estão dentro dos limites da imagem.
        if (x < 0 || x >= trackWidth || y < 0 || y >= trackHeight) {
            return true; // Considera que é uma colisão se estiver fora dos limites da imagem.
        }

        // Verifica se o carro colidiu com alguma área fora da pista (p.ex., cor preta).
        int pixelColor = trackBitmap.getPixel((int) x, (int) y);
        return pixelColor == Color.BLACK; // Consideramos cor preta como fora da pista.
    }

    public Rect getStartLineRect() {
        return startLine;
    }
}
