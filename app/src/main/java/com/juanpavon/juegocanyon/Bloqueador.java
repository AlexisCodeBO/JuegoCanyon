package com.juanpavon.juegocanyon;

public class Bloqueador extends ElementoJuego {
    private int penalizacionFallo;

    // Constructor
    public Bloqueador(CanyonView vista, int color, int penalizacionFallo, int x, int y,
                      int anchura, int longitud, float velocidadY) {
        // Llamar al constructor de la clase madre para que construya este elemnto del juego
        super(vista, color, CanyonView.ID_SONIDO_BLOQUEADOR, x, y, anchura, longitud, velocidadY);
        this.penalizacionFallo = penalizacionFallo;
    } // Bloqueador()

    public int obtenerPenalizacionFallo() {
        return penalizacionFallo;
    } // obtenerPenalizacionFallo()
} //Bloqueador
