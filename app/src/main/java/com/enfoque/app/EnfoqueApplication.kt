package com.enfoque.app

import android.app.Application
import com.enfoque.app.data.temporizador.Notificaciones
import com.enfoque.app.di.ContenedorApp
import com.enfoque.app.di.ContenedorAppImpl

class EnfoqueApplication : Application() {

    lateinit var contenedor: ContenedorApp
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = ContenedorAppImpl(this)
        Notificaciones.crearCanales(this)
    }
}
