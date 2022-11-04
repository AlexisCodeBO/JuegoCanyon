package com.juanpavon.juegocanyon;

import android.media.AudioManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainActivityFragment extends Fragment {

    private CanyonView canyonView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Obtener una referencia al layout de fragment_main una vez que se ha inflado
        View vista = inflater.inflate(R.layout.fragment_main, container, false);

        canyonView = vista.findViewById(R.id.canyonView);

        return vista;
    } // onCreateView

    // Configurar el control de volumen cuando se cree la actividad
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Permitir que el volumen del juego sea controlado por los botones del dispositivo
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
    } // onActivityCreated()

    // Cuando la actividad esté en pausa, detener el juego
    @Override
    public void onPause() {
        super.onPause();
        canyonView.detenerJuego();
    } // onPause()

    // Cuando la actividad vaya a ser destruida, liberar los recursos del juego
    @Override
    public void onDestroy() {
        super.onDestroy();
        canyonView.liberarRecursos();
    } // onDestroy()
} // MainActivityFragment
