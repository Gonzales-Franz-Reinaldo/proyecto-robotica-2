package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.features

import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

/**
 * Contador con Ventana Temporal Deslizante
 *
 * Equivalente a: drowsiness_processor/drowsiness_features/yawn/processing.py
 *
 * Mantiene un registro de timestamps de eventos y descarta los que están
 * fuera de la ventana temporal especificada.
 */
class WindowedCounterUseCase @Inject constructor() {

    data class WindowConfig(
        val windowDurationMs: Long,
        val threshold: Int
    )

    private val eventTimestamps = ConcurrentLinkedQueue<Long>()

    /**
     * Agregar un nuevo evento
     *
     * @param currentTime Timestamp actual
     * @param config Configuración de ventana
     * @return Par (exceedsThreshold, currentCount)
     */
    fun addEvent(currentTime: Long, config: WindowConfig): Pair<Boolean, Int> {
        // Agregar nuevo evento
        eventTimestamps.add(currentTime)

        // Limpiar eventos fuera de ventana
        cleanOldEvents(currentTime, config.windowDurationMs)

        // Evaluar umbral
        val currentCount = eventTimestamps.size
        val exceedsThreshold = currentCount > config.threshold

        return Pair(exceedsThreshold, currentCount)
    }

    /**
     * Obtener conteo actual sin agregar evento
     */
    fun getCurrentCount(currentTime: Long, windowDurationMs: Long): Int {
        cleanOldEvents(currentTime, windowDurationMs)
        return eventTimestamps.size
    }

    /**
     * Limpiar eventos fuera de ventana
     */
    private fun cleanOldEvents(currentTime: Long, windowDurationMs: Long) {
        val cutoffTime = currentTime - windowDurationMs

        // Eliminar eventos antiguos
        while (eventTimestamps.isNotEmpty() && eventTimestamps.peek() < cutoffTime) {
            eventTimestamps.poll()
        }
    }

    /**
     * Resetear contador
     */
    fun reset() {
        eventTimestamps.clear()
    }
}