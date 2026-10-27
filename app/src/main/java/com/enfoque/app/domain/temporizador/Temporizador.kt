package com.enfoque.app.domain.temporizador

import com.enfoque.app.domain.model.Tarea

/** Abstracción de la ejecución de un bloque de enfoque con duración. */
interface Temporizador {

    /**
     * Arranca el servicio en segundo plano que programa la alarma y notifica al terminar.
     * @return false si la tarea no tiene una duración válida o el sistema rechazó el inicio.
     */
    fun iniciar(tarea: Tarea): Boolean
}
