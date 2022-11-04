package com.juanpavon.juegocanyon;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class Canyon {
    private int radioBase;
    private int longitudBarril;
    private Point finalBarril = new Point(); // el punto donde termina el barril
    private double anguloBarril; // en radianes
    private BalaCanyon balaCanyon;
    private Paint pintura = new Paint(); // para dibujar el cañón
    private CanyonView vista; // vista donde se muestra el cañón

    public Canyon(CanyonView vista, int radioBase, int longitudBarril, int anchuraBarril) {
        this.vista = vista;
        this.radioBase = radioBase;
        this.longitudBarril = longitudBarril;
        pintura.setStrokeWidth(anchuraBarril); // define el ancho de la línea que dibuja el barril
        pintura.setColor(Color.BLACK);
        alinear(Math.PI/2);
    } // Canyon()

    public void alinear(double anguloBarril) {
        this.anguloBarril = anguloBarril;
        // Se calcula el final del barril con trigonometría
        finalBarril.x = (int) (longitudBarril*Math.sin(anguloBarril));
        finalBarril.y = (int) (-longitudBarril*Math.cos(anguloBarril)) +
                vista.obtenerAlturaPantalla()/2;
    } // alinear()

    // Crea y dispara una bala del cañón en la dirección que apunta el barril
    public void dispararBalaCanyon() {
        // Calcular los componentes (x, y) de la velocidad de la bala
        int velocidadX = (int) (CanyonView.PORCENTAJE_VELOCIDAD_BALACANYON*
                vista.obtenerAnchuraPantalla()*Math.sin(anguloBarril));
        int velocidadY = (int) (CanyonView.PORCENTAJE_VELOCIDAD_BALACANYON*
                vista.obtenerAnchuraPantalla()*-Math.cos(anguloBarril));

        int radio = (int) (CanyonView.PORCENTAJE_RADIO_BALACANYON*vista.obtenerAlturaPantalla());

        balaCanyon = new BalaCanyon(vista, Color.BLACK, CanyonView.ID_SONIDO_DISPARO, -radio,
                vista.obtenerAlturaPantalla()/2 - radio, radio, velocidadX, velocidadY);

        balaCanyon.reproducirSonido(); // reproducir el sonido de disparo
    } // dispararBalaCanyon()

    public void dibujar(Canvas lienzo) {
        // Dibuja el barril del cañón
        lienzo.drawLine(0, vista.obtenerAlturaPantalla()/2, finalBarril.x, finalBarril.y,
                pintura);
        // Dibuja la base del cañón, mitad del círculo se dibuja fuera de la pantalla
        lienzo.drawCircle(0, vista.obtenerAlturaPantalla()/2, radioBase, pintura);
    } // dibujar()

    // Devuelve la bala que disparó el cañón
    public BalaCanyon obtenerBalaCanyon() {
        return  balaCanyon;
    } // obtenerBalaCanyon()

    // Remueve la bala del juego
    public void removerBalaCanyon() {
        balaCanyon = null;
    } // obtenerBalaCanyon()
} // Canyon
