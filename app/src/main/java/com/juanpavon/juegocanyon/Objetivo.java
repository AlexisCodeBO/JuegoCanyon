package com.juanpavon.juegocanyon;

public class Objetivo extends ElementoJuego {
    private int recompensaColision;

    // Constructor
    public Objetivo(CanyonView vista, int color, int recompensaColision, int x, int y, int anchura,
                    int longitud, float velocidadY) {
        // Crear el Objetivo con el constructor de la clase madre
        super(vista, color, CanyonView.ID_SONIDO_OBJETIVO, x, y, anchura, longitud, velocidadY);
        this.recompensaColision = recompensaColision;
    } // Objetivo()

    public int obtenerRecompensaColision() {
        return recompensaColision;
    } // obtenerRecompensaColision()
} // Objetivo
