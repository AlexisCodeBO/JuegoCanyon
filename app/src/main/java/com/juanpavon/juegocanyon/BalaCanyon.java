package com.juanpavon.juegocanyon;

import android.graphics.Canvas;
import android.graphics.Rect;

public class BalaCanyon extends ElementoJuego {
    private float velocidadX; // para el movimiento horizontal de la bala
    private boolean enPantalla; // determina si hay una bala en pantalla

    // Constructor
    public BalaCanyon(CanyonView vista, int color, int idSonido, int x, int y, int radio,
                      float velocidadX, float velocidadY) {
        super(vista, color, idSonido, x, y, 2*radio, 2*radio, velocidadY);
        this.velocidadX = velocidadX;
        enPantalla = true;
    } // BalaCanyon()

    private int obtenerRadio() {
        return (forma.right - forma.left)/2;
    } // obtenerRadio()

    // Devuelve verdadero si la bala choca con un elemento del juego dado
    public boolean chocaCon(ElementoJuego elemento) {
        // El método intersects devuelve true si dos rectángulos se tocan en al menos un píxel
        return Rect.intersects(forma, elemento.forma) && velocidadX > 0;
    } // chocaCon()

    // Devuelve verdad si la bala está en la pantalla
    public boolean estaEnPantalla() {
        return enPantalla;
    } // estaEnPantalla()

    // Invierte la velocidad horizontal de la bala (se usa cuando choca con el bloqueador)
    public void invertirVelocidadX() {
        velocidadX *= -1;
    } // invertirVelocidadX()

    // Actualiza la posición de la bala
    @Override
    public void actualizar(double intervalo) {
        super.actualizar(intervalo); // actuliza la posición vertical de la bala

        // Actualizar la posición horizontal
        forma.offset((int) (velocidadX*intervalo), 0); // e = vt

        // Si la bala sale de la pantalla
        if (forma.left < 0 || forma.top < 0 || forma.right > vista.obtenerAnchuraPantalla() ||
                forma.bottom > vista.obtenerAlturaPantalla()) {
            enPantalla = false;
        }
    } // actualizar()

    // Sobreescribe el método dibujar para que dibuje un círculo
    @Override
    public void dibujar(Canvas lienzo) {
        lienzo.drawCircle(forma.left + obtenerRadio(), forma.top + obtenerRadio(),
                obtenerRadio(), pintura);
    } // dibujar()
} // BalaCanyon
