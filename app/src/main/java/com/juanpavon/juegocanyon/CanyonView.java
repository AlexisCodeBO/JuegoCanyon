package com.juanpavon.juegocanyon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Random;

public class CanyonView extends SurfaceView implements SurfaceHolder.Callback {
    // Para el registro de errores
    private static final String ETIQUETA = "com.juanpavon.juegocanyon.ETIQUETA";
    private static final int VERSION = Build.VERSION.SDK_INT; // obtiene la versión del SO Android

    public static final int PENALIZACION_FALLO = 2; // segundos restados en caso de fallo
    public static final int RECOMPENSA_COLISION = 3; // secgundos sumados encaso de acierto

    // Constantes para el cañón
    public static final double PORCENTAJE_RADIO_BASE_CANYON = 3.0/40;
    public static final double PORCENTAJE_ANCHURA_BARRIL_CANYON = 3.0/40;
    public static final double PORCENTAJE_LONGITUD_BARRIL_CANYON = 1.0/10;

    // Constantes para la bala del cañón
    public static final double PORCENTAJE_VELOCIDAD_BALACANYON = 3.0/2;
    public static final double PORCENTAJE_RADIO_BALACANYON = 3.0/80;

    // Constantes para los objetivos
    public static final double PORCENTAJE_ANCHURA_OBJETIVO = 1.0/40;
    public static final double PORCENTAJE_LONGITUD_OBJETIVO = 3.0/20;
    public static final double PORCENTAJE_X_PRIMER_OBJETIVO = 3.0/5;
    public static final double PORCENTAJE_ESPACIADO_OBJETIVOS = 1.0/60;
    public static final double PIEZAS_OBJETIVO = 9;
    public static final double PORCENTAJE_VELOCIDAD_MIN_OBJETIVO = 3.0/4;
    public static final double PORCENTAJE_VELOCIDAD_MAX_OBJETIVO = 6.0/4;

    // Constantes para el bloqueador
    public static final double PORCENTAJE_ANCHURA_BLOQUEADOR = 1.0/40;
    public static final double PORCENTAJE_LONGITUD_BLOQUEADOR = 1.0/4;
    public static final double PORCENTAJE_X_BLOQUEADOR = 1.0/2;
    public static final double PORCENTAJE_VELOCIDAD_BLOQUEADOR = 1.0;

    // El tamaño del texto es 1/18 del ancho de la pantalla
    public static final double PORCENTAJE_TAMANYO_TEXTO = 1.0/18;

    private HiloCanyon hiloCanyon; // controla el ciclo del juego
    private Activity actividad; // para mostrar el diálogo de Game Over en el hilo de la GUI
    private boolean seMuestraDialogo; // para pausar el juego se hay un diálogo en pantalla

    // Objetos del juego
    private Canyon canyon;
    private Bloqueador bloqueador;
    private ArrayList<Objetivo> objetivos;

    //  Variables de dimensión
    private int anchuraPantalla;
    private int alturaPantalla;

    // Variables para el ciclo del juego y sus estadísticas
    private boolean gameOver; // determina si el juego está finalizado o no
    private double tiempoRestante; // tiempo que le queda al jugador (inicialmente 10 s)
    private int disparosRealizados;
    private double tiempoTotalTranscurrido; // tiempo desde el inicio del juego

    // Constantes y variables para administrar los sonidos
    public static final int ID_SONIDO_BLOQUEADOR = 0;
    public static final int ID_SONIDO_OBJETIVO = 1;
    public static final int ID_SONIDO_DISPARO = 2;
    private SoundPool bancoSonidos; // reproduce los efectos de sonido
    private SparseIntArray mapaSonidos; // mapea lod IDs al banco de sonidos

    // Varaibles Paint para dibujar los elementos del juego en pantalla
    private Paint pinturaTexto;
    private Paint pinturaFondo;

    // Constructor
    public CanyonView(Context context, AttributeSet attrs) {
        super(context, attrs); // llamada al constructor de la superclase (SurfaceView)
        actividad = (Activity) getContext(); // guardar una referencia a MainActivity

        getHolder().addCallback(this); // registrar el oyente para el SurfaceHolder.Callback

        if (VERSION >= Build.VERSION_CODES.LOLLIPOP) { // para la API 21 o superior
            // Configurar los atributos para el audio del juego
            AudioAttributes.Builder constructorAtributos = new AudioAttributes.Builder();
            constructorAtributos.setUsage(AudioAttributes.USAGE_GAME);

            // Inicializar el banco de sonidos para reproducir los tres efectos de sonido
            SoundPool.Builder constructor = new SoundPool.Builder();
            constructor.setMaxStreams(1); // un flujo porque sólo se reprodue un sonido a la vez
            constructor.setAudioAttributes(constructorAtributos.build());
            bancoSonidos = constructor.build();
        }
        else { // para versiones de Android anteriores a Lollipop
            bancoSonidos = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }

        // Crear el mapa de sonidos y precargar los sonidos
        mapaSonidos = new SparseIntArray(3); // capacidad para tres sonidos
        mapaSonidos.put(ID_SONIDO_BLOQUEADOR, bancoSonidos.load(context, R.raw.colision_bloqueador,
                1));
        mapaSonidos.put(ID_SONIDO_OBJETIVO, bancoSonidos.load(context, R.raw.colision_objetivo,
                1));
        mapaSonidos.put(ID_SONIDO_DISPARO, bancoSonidos.load(context, R.raw.disparo_canyon,
                1));

        pinturaTexto = new Paint();
        pinturaFondo = new Paint();
        pinturaFondo.setColor(Color.WHITE);
    } // CanyonView()

    // Se llama cuando cambia el tamaño del SurfaceView, por ejemplo cuando el SurfaceView se
    // agrega a la jerarquía de vistas
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        anchuraPantalla = w; // guardar el ancho de CanyonView
        alturaPantalla = h; // guardar el alto de CanyonView

        // Configurar las propiedades del texto
        pinturaTexto.setTextSize((int) (PORCENTAJE_TAMANYO_TEXTO*alturaPantalla));
        pinturaTexto.setAntiAlias(true); // suaviza el borde del texto
    } // onSizeChanged()

    public int obtenerAlturaPantalla() {
        return alturaPantalla;
    } // obtenerAlturaPantalla()

    public int obtenerAnchuraPantalla() {
        return anchuraPantalla;
    } // obtenerAnchuraPantalla()

    public void reproducirSonido(int idSonido) {
        bancoSonidos.play(mapaSonidos.get(idSonido), 1,1,1, 0,
                1f);
    } // reproducirSonido()

    // Reinicia todos los elemntos de la pantalla e inicia un juego nuevo
    public void juegoNuevo() {
        // Construir un nuevo cañón
        canyon = new Canyon(this, (int) (PORCENTAJE_RADIO_BASE_CANYON*alturaPantalla),
                (int) (PORCENTAJE_LONGITUD_BARRIL_CANYON*anchuraPantalla),
                (int) (PORCENTAJE_ANCHURA_BARRIL_CANYON*alturaPantalla));

        Random rng = new Random(); // para definir velocidades aleatoriamente
        objetivos = new ArrayList<>();

        // Inicialmente objetivoX guarda la posicón horizontal del primer objetivo a la izquierda
        int objetivoX = (int) (PORCENTAJE_X_PRIMER_OBJETIVO*anchuraPantalla);

        // Calcular la coordenada Y de los objetivos
        int objetivoY = (int) ((0.5 - PORCENTAJE_LONGITUD_OBJETIVO/2)*alturaPantalla);

        // Agregar PIEZAS_OBJETIVO=9 a la lista de objetivos
        for (int n = 0; n < PIEZAS_OBJETIVO; n++) {
            // Definir una velocidad aleatoria para el objetivo n, entre los valores MIN y MAX
            double velocidad = alturaPantalla * (rng.nextDouble() *
                    (PORCENTAJE_VELOCIDAD_MAX_OBJETIVO - PORCENTAJE_VELOCIDAD_MIN_OBJETIVO) +
                    PORCENTAJE_VELOCIDAD_MIN_OBJETIVO);

            // Alternar los colores de los objetivos entre claro y oscuro
            int color;
            if (VERSION >= Build.VERSION_CODES.M) {
                color = (n % 2 == 0) ? getResources().getColor(R.color.claro, getContext().getTheme())
                        : getResources().getColor(R.color.oscuro, getContext().getTheme());
            }
            else {
                color = (n % 2 == 0) ? ContextCompat.getColor(getContext(), R.color.claro) :
                        ContextCompat.getColor(getContext(), R.color.oscuro);
            }

            velocidad *= -1; // invertir la velocidad para el siguiente objetivo

            // Crear y agregar un nuevo objetivo a la lista de objetivos
            objetivos.add(new Objetivo(this, color, RECOMPENSA_COLISION, objetivoX, objetivoY,
                    (int) (PORCENTAJE_ANCHURA_OBJETIVO*anchuraPantalla),
                    (int) (PORCENTAJE_LONGITUD_OBJETIVO*alturaPantalla), (float) velocidad));

            // Incrementar la coordenada x del siguiente objetivo, para que aparezca más a la der.
            objetivoX += (PORCENTAJE_ANCHURA_OBJETIVO + PORCENTAJE_ESPACIADO_OBJETIVOS) *
                    anchuraPantalla;
        }

        // Crear un bloqueador nuevo
        bloqueador = new Bloqueador(this, Color.BLACK, PENALIZACION_FALLO,
                (int) (PORCENTAJE_X_BLOQUEADOR*anchuraPantalla),
                (int) ((0.5 - PORCENTAJE_LONGITUD_BLOQUEADOR/2) * alturaPantalla),
                (int) (PORCENTAJE_ANCHURA_BLOQUEADOR*anchuraPantalla),
                (int) (PORCENTAJE_LONGITUD_BLOQUEADOR*alturaPantalla),
                (float) (PORCENTAJE_VELOCIDAD_BLOQUEADOR*alturaPantalla));

        tiempoRestante = 10; // comenzar la cuenta regresiva en 10 s

        disparosRealizados = 0; // establecer la catidad inical de disparos
        tiempoTotalTranscurrido = 0.0; // establecer el tiempo trancurrido a cero

        if (gameOver) { // iniciar un juego nuevo después de terminar un juego previo
            gameOver = false;
            hiloCanyon = new HiloCanyon(getHolder()); // crear el hilo
            hiloCanyon.start(); // iniciar el hilo del bucle del juego
        }

        ocultarBarrasSistema();
    } // juegoNuevo()

    // Llamado repetidamente por el hilo del CanyonView para actualizar las posiciones de los
    // elementos del juego
    private void actualizarPosiciones(double tiempoTranscurridoMS) {
        double intervalo = tiempoTranscurridoMS / 1000.0; // calcular el tiempo en segundos

        if (canyon.obtenerBalaCanyon() != null) {
            canyon.obtenerBalaCanyon().actualizar(intervalo); // actualizar la posición de la bala
        }

        bloqueador.actualizar(intervalo); // actualizar la posición del bloqueador

        for (ElementoJuego objetivo : objetivos) {
            objetivo.actualizar(intervalo); // actualizar la posición del objetivo
        }

        tiempoRestante -= intervalo; // restar del tiempo que queda

        // El juguador pierde cuando el tiempo llega a cero
        if (tiempoRestante <= 0.0) {
            tiempoRestante = 0.0;
            gameOver = true;
            hiloCanyon.establecerEjecucion(false);
            mostrarDialogoGameOver(R.string.perdiste);
        }

        // El jugador gana cuando destruye todos los objetivos
        if (objetivos.isEmpty()) { // isEmpty() devuelve true si la lista está vacía
            gameOver = true;
            hiloCanyon.establecerEjecucion(false);
            mostrarDialogoGameOver(R.string.ganaste);
        }
    } // actualizarPosiciones()

    // Alinea el barril y dispara una bala si es que no hay actualmente una bala en pantalla
    public void alinearYDispararBala(MotionEvent evento) {
        Point puntoToque = new Point((int) (evento.getX()), (int) (evento.getY()));

        // Calcular la distacia del punto con respecto a la mitad vertical de la pantalla
        double centroMenosY = alturaPantalla/2 - puntoToque.y;

        // Calcular el ángulo que hace el barril con la horizontal
        double angulo = Math.atan2(puntoToque.x, centroMenosY);

        // Apuntar el barril al lugar donde se tocó la pantalla
        canyon.alinear(angulo);

        // Si no hay una bala en la pantalla, disparar una bala
        if (canyon.obtenerBalaCanyon() == null || !canyon.obtenerBalaCanyon().estaEnPantalla()) {
            canyon.dispararBalaCanyon();
            disparosRealizados++;
        }
    } // alinearYDispararBala()

    // Por seguridad los ids de recursos se seben manejar como constantes
    private void mostrarDialogoGameOver(final int idMensaje) {
        final AlertDialog.Builder constructor = new AlertDialog.Builder(getContext());
        constructor.setTitle(getResources().getString(idMensaje));

        constructor.setMessage(getResources().getString(R.string.formato_resultados,
                disparosRealizados, tiempoTotalTranscurrido));

        constructor.setPositiveButton(R.string.reiniciar_juego, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                seMuestraDialogo = false;
                juegoNuevo();
            } // onClick()
        });

        // Se le indica a la actividad que muestre el diálogo en el hilo de la GUI
        actividad.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mostrarBarrasSistema();
                seMuestraDialogo = true;
                constructor.setCancelable(false);
                constructor.show();
            } // run()
        });
    } // mostrarDialogoGameOver()

    // Dibuja los elementos elemtnos del juego en el Canvas dado
    public void dibujarElementosJuego(Canvas lienzo) {
        // Limpiar el fondo
        lienzo.drawRect(0,0, obtenerAnchuraPantalla(), obtenerAlturaPantalla(), pinturaFondo);

        // Mostrar el tiempo restante
        lienzo.drawText(getResources().getString(R.string.formato_tiempo_restante, tiempoRestante),
                50, 100, pinturaTexto);

        // Dibujar el cañón
        canyon.dibujar(lienzo);

        BalaCanyon balaCanyon = canyon.obtenerBalaCanyon();
        // Dibujar la bala si fue disparda y está en pantalla
        if (balaCanyon != null && balaCanyon.estaEnPantalla())
            balaCanyon.dibujar(lienzo);

        bloqueador.dibujar(lienzo); // dibujar el bloqeuador

        // Dibujar todos los objetivos
        for (ElementoJuego objetivo : objetivos)
            objetivo.dibujar(lienzo);
    } // dibujarElementosJuego()

    // Verifica si la bal choca con el bloqueador a alguno de los objetivos y maneja las colisiones
    public void verificarColisiones() {
        BalaCanyon balaCanyon = canyon.obtenerBalaCanyon();

        if (balaCanyon != null && balaCanyon.estaEnPantalla()) {
            // Remover cualquier objetivo que haya chocado con la bala
            for  (int n = 0; n < objetivos.size(); n++) {
                if (balaCanyon.chocaCon(objetivos.get(n))) {
                    objetivos.get(n).reproducirSonido(); // reproducir el efecto de sonido

                    // Añadir los segundos de recompensa al tiempo restante
                    tiempoRestante += objetivos.get(n).obtenerRecompensaColision();

                    canyon.removerBalaCanyon(); // eliminar la bala
                    objetivos.remove(n); // eliminar el objetivo
                    n--; // para evitar tratar de acceder a un elemnto que no existe
                }
            }

            // Verificar si la bala choca con el bloqueador
            if (balaCanyon.chocaCon(bloqueador)) {
                bloqueador.reproducirSonido(); // reproducir el efecto de sonido

                balaCanyon.invertirVelocidadX(); // la bala rebota contra el bloqueador

                // Quitar segundos al tiempo restante
                tiempoRestante -= bloqueador.obtenerPenalizacionFallo();
            }
        }
        else {
            canyon.removerBalaCanyon();
        }
    } // verificarColisiones()

    // Detiene el juego cuando se llama al método onPause() de MainActivityFragment
    public void detenerJuego() {
        if (hiloCanyon != null)
            hiloCanyon.establecerEjecucion(false); // indicar al hilo que se detenga
    } // detenerJuego()

    // Llamado en el método onDestroy() de MainActivityFragment
    public void liberarRecursos() {
        bancoSonidos.release(); // liberar los recursos utilizados por el SoundPool
        bancoSonidos = null;
    } // liberarRecursos()

    // Llamado cuando la superficie cambia de tamaño
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    } // surfaceChanged

    // Llamado cuado la superficie se crea por primera
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!seMuestraDialogo) {
            juegoNuevo();
            hiloCanyon = new HiloCanyon(holder); // crear el hilo
            hiloCanyon.establecerEjecucion(true); // indicar al hilo que su estado es de ejecución
            hiloCanyon.start(); // iniciar el hilo
        }
    } // surfaceCreated()

    // Llamado cuando la superficie se destruye
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Asegurarse de que el hilo está terminando de manera adecuada
        boolean tratarNuevamente = true;
        hiloCanyon.establecerEjecucion(false); // terminar el hiloCanyon

        while (tratarNuevamente) {
            try {
                hiloCanyon.join(); // esperar a que hiloCanyon termine
                tratarNuevamente = false;
            }
            catch (InterruptedException e) {
                Log.wtf(ETIQUETA, "Hilo interrumpido", e);
            }
        }
    } // surfaceDestroyed

    // Llamado cuando el jugador toca la pantalla
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int accion = event.getAction();

        // Si el jugador toca o mueve el dedo sobre la pantalla
        if (accion == MotionEvent.ACTION_DOWN || accion == MotionEvent.ACTION_MOVE)
            alinearYDispararBala(event); // alinear y disparar la bala

        return true; // indicar que ya se manejó el evento
    } // onTouchEvent()

    // Clase interior privada para controlar el ciclo del juego
    private class HiloCanyon extends Thread {
        private SurfaceHolder surfaceHolder; // para manioular el lienzo (Canvas)
        private boolean hiloEnEjecucion = true; // en ejecución de manera predeterminada

        // Constructor para inicializar el SurfaceHolder
        public HiloCanyon(SurfaceHolder holder) {
            surfaceHolder = holder;
            setName("HiloCanyon"); // asignar un nombre al hilo
        } // HiloCanyon()

        public void establecerEjecucion(boolean ejecucion) {
            hiloEnEjecucion = ejecucion;
        } // establecerEjecucion

        // Controlar el ciclo del juego
        @Override
        public void run() {
            Canvas lienzo = null;
            long tiempoCuadroPrevio = System.currentTimeMillis();

            while (hiloEnEjecucion) {
                try {
                    // obtener acceso exclusivo al lienzo para que el hilo dibuje en él
                    lienzo = surfaceHolder.lockCanvas(null);

                    // Acceso exclusico surfaceHolder para dibujar
                    synchronized (surfaceHolder) {
                        long tiempoActual = System.currentTimeMillis();
                        double tiempoTrancurridoMS = tiempoActual - tiempoCuadroPrevio;
                        tiempoTotalTranscurrido += tiempoTrancurridoMS/1000.0;
                        actualizarPosiciones(tiempoTrancurridoMS); // actulizar el estado del juego
                        verificarColisiones();
                        dibujarElementosJuego(lienzo);
                        tiempoCuadroPrevio = tiempoActual; // guardar el tiempoActual como el tiempo
                                                            // del cuadro previo
                    }
                }
                finally { // el finally ocurre indempendientemente de haya un error o no
                    // Mostrar el contenido del lienzo en el CanyonView y permitir que otro hilos
                    // utilicen el lienzo
                    if (lienzo != null)
                        surfaceHolder.unlockCanvasAndPost(lienzo);
                }
            }
        } // run()
    } // HiloCanyon

    // Ocultar las barras del sistema y la barra de la app
    private void ocultarBarrasSistema() {
        if (VERSION >= Build.VERSION_CODES.KITKAT)
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE);
    } // ocultarBarrasSistema()

    // Mostrar las barras del sistema y la barra de la app
    private void mostrarBarrasSistema() {
        if (VERSION >= Build.VERSION_CODES.KITKAT)
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    } // mostrarBarrasSistema()
} // SurfaceView
