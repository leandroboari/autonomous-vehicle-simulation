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

    // Bitmap que representa a imagem da pista.
    private final Bitmap trackBitmap;

    // Retângulo que define a linha de partida/chegada na pista.
    private final Rect startLine;

    // Objeto Paint usado para desenhar a linha de largada/chegada.
    private final Paint linePaint;

    // Largura e altura da pista.
    private final int trackWidth, trackHeight;

    // Construtor da classe Track. Ele configura a pista, redimensiona a imagem e define a linha de largada.
    public Track(
            Context context,
            int trackResourceId, // ID do recurso da imagem da pista.
            int trackWidth,      // Largura desejada da pista.
            int trackHeight,     // Altura desejada da pista.
            int startLineX1,     // Coordenada X1 da linha de largada.
            int startLineY1,     // Coordenada Y1 da linha de largada.
            int startLineX2,     // Coordenada X2 da linha de largada.
            int startLineY2      // Coordenada Y2 da linha de largada.
    ) {
        // Define a largura e altura da pista a partir dos valores fornecidos.
        this.trackWidth = trackWidth;
        this.trackHeight = trackHeight;

        // Carrega a imagem da pista dos recursos do aplicativo.
        Bitmap originalBitmap = BitmapFactory.decodeResource(
                context.getResources(),
                trackResourceId
        );

        // Redimensiona o bitmap da pista para o tamanho especificado (trackWidth x trackHeight).
        trackBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                trackWidth,
                trackHeight,
                false
        );

        // Define a linha de largada/chegada da pista como um retângulo.
        startLine = new Rect(
                startLineX1, // Coordenada X1
                startLineY1, // Coordenada Y1
                startLineX2, // Coordenada X2
                startLineY2  // Coordenada Y2
        );

        // Inicializa o Paint para desenhar a linha de largada/chegada.
        linePaint = new Paint();

        // Define a cor da linha como verde escuro (padrão do Android para holo_green_dark).
        linePaint.setColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));

        // Define a espessura da linha.
        linePaint.setStrokeWidth(5);
    }

    // Função responsável por desenhar a pista e a linha de largada/chegada no canvas.
    public void draw(Canvas canvas) {
        // Desenha o bitmap da pista no canvas, começando da posição (0, 0).
        canvas.drawBitmap(trackBitmap, 0, 0, null);

        // Desenha a linha de largada/chegada no canvas usando as coordenadas do Rect startLine.
        canvas.drawLine(
                startLine.left,   // Posição inicial X da linha.
                startLine.top,    // Posição inicial Y da linha.
                startLine.right,  // Posição final X da linha.
                startLine.bottom, // Posição final Y da linha.
                linePaint         // Configuração de cor e espessura.
        );
    }

    // Função que verifica colisões na pista.
    public boolean isCollision(float x, float y) {
        // Verifica se as coordenadas (x, y) estão fora dos limites da imagem da pista.
        if (x < 0 || x >= trackWidth || y < 0 || y >= trackHeight) {
            return true; // Se estiver fora dos limites, considera como colisão.
        }

        // Verifica a cor do pixel na posição (x, y) da pista.
        int pixelColor = trackBitmap.getPixel((int) x, (int) y);

        // Se a cor do pixel for preta, considera que o carro está fora da pista (colisão).
        return pixelColor == Color.BLACK;
    }

    // Retorna o retângulo que define a linha de largada/chegada.
    public Rect getStartLineRect() {
        return startLine;
    }
}
