package com.juanpavon.juegocanyon;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class ElementoJuego {
    protected CanyonView vista; // la vista que contiene este elemento del juego
    protected Paint pintura; // para dibujar este elemento del juego
    protected Rect forma; // el rectángulo que delimita este elemento del juego
    private float velocidadY; // la velocidad vertical de este elemento del juego
    private int idSonido; // representa el sonido asociado a este elemento del juego

    // Constructor público de la clase
    public ElementoJuego(CanyonView vista, int color, int idSonido, int x, int y, int anchura,
                         int longitud, float velocidadY) {
        this.vista = vista;
        pintura = new Paint();
        pintura.setColor(color);
        forma = new Rect(x, y, x + anchura, y + longitud);
        this.idSonido = idSonido;
        this.velocidadY = velocidadY;
    } // ElementoJuego()

    // Actualiza la posición del elemento del juego de acuerdo al tiempo transcurrido
    public void actualizar(double intervalo) {
        // Actualizar la posición vertical
        forma.offset(0, (int) (velocidadY * intervalo)); // e = vt

        // Si el elemnto choca con alguno de los bordes de la pantalla
        if (forma.top < 0 && velocidadY < 0 || forma.bottom > vista.obtenerAlturaPantalla() &&
                velocidadY > 0)
            velocidadY *= -1; // invertir la velocidad del elemento
    } // actualizar()

    // Dibuja el elemento en un Canvas dado
    public void dibujar(Canvas lienzo) {
        lienzo.drawRect(forma, pintura);
    } // dibujar()

    public void reproducirSonido() {
        vista.reproducirSonido(idSonido);
    } // reproducirSonido()
} // ElementoJuego
