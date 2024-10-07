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

    private Bitmap trackBitmap;
    private Rect startLine; // A linha de partida/chegada da pista
    private Paint linePaint;

    public Track(Context context, int trackResourceId, int startLineX1, int startLineY1, int startLineX2, int startLineY2) {
        // Carrega a imagem da pista a partir de recursos.
        trackBitmap = BitmapFactory.decodeResource(context.getResources(), trackResourceId);

        // Define as coordenadas da linha de largada/chegada.
        startLine = new Rect(startLineX1, startLineY1, startLineX2, startLineY2);

        // Inicializa a pintura da linha de largada/chegada.
        linePaint = new Paint();
        linePaint.setColor(ContextCompat.getColor(context, android.R.color.holo_green_dark)); // Cor da linha verde.
        linePaint.setStrokeWidth(5); // Espessura da linha.
    }

    public void draw(Canvas canvas) {
        // Obtém as dimensões originais da pista
        int trackWidth = trackBitmap.getWidth();
        int trackHeight = trackBitmap.getHeight();

        // Desenha a pista no tamanho original (sem redimensionamento)
        canvas.drawBitmap(trackBitmap, 0, 0, null);

        // Desenha a linha de largada/chegada no local correto, ajustada para a escala original
        // Aqui vamos garantir que a linha de largada esteja na posição desejada, que pode ser ajustada manualmente
        canvas.drawLine(startLine.left, startLine.top, startLine.right, startLine.bottom, linePaint);
    }


    public boolean isCollision(float x, float y) {
        // Verifica se as coordenadas estão dentro dos limites da imagem.
        if (x < 0 || x >= trackBitmap.getWidth() || y < 0 || y >= trackBitmap.getHeight()) {
            return true; // Considera que é uma colisão se estiver fora dos limites da imagem.
        }

        // Verifica se o carro colidiu com alguma área fora da pista (p.ex., cor preta).
        int pixelColor = trackBitmap.getPixel((int) x, (int) y);
        return pixelColor == Color.BLACK; // Consideramos cor preta como fora da pista.
    }


    public boolean checkLap(float x, float y) {
        // Verifica se o carro passou pela linha de largada/chegada.
        return startLine.contains((int) x, (int) y);
    }
}
