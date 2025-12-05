package com.example.driverdrowsinessdetectorapp.domain.model

data class MetricasSomnolencia(
    val timestamp: Long,
    
    // Métricas básicas
    val ear: Float,
    val mar: Float,
    val headPose: HeadPose,
    
    //  PARPADEO 
    val isBlinking: Boolean,
    val blinkCount: Int,
    
    // Microsueño
    val isMicrosleep: Boolean,
    val microsleepCount: Int,
    val microsleepDurations: List<Long>,
    
    // Bostezo
    val isYawning: Boolean,
    val yawnCount: Int,
    val yawnDurations: List<Long>,
    
    // Cabeceo
    val isNodding: Boolean,
    val noddingCount: Int,
    val noddingDurations: List<Long>,
    
    // Frotamiento de ojos
    val eyeRubFirstHand: Triple<Boolean, Int, List<Long>>,
    val eyeRubSecondHand: Triple<Boolean, Int, List<Long>>,
    
    // Alertas
    val alertLevel: AlertLevel,
    val alertType: AlertType?
) {
    // PROPIEDADES COMPUTADAS para acceso directo en UI
    val eyeRubFirstHandCount: Int
        get() = eyeRubFirstHand.second

    val eyeRubSecondHandCount: Int
        get() = eyeRubSecondHand.second

    companion object {
        fun empty() = MetricasSomnolencia(
            timestamp = 0L,
            ear = 0f,
            mar = 0f,
            headPose = HeadPose(pitch = 0f, yaw = 0f, roll = 0f),
            isBlinking = false,
            blinkCount = 0,
            isMicrosleep = false,
            microsleepCount = 0,
            microsleepDurations = emptyList(),
            isYawning = false,
            yawnCount = 0,
            yawnDurations = emptyList(),
            isNodding = false,
            noddingCount = 0,
            noddingDurations = emptyList(),
            eyeRubFirstHand = Triple(false, 0, emptyList()),
            eyeRubSecondHand = Triple(false, 0, emptyList()),
            alertLevel = AlertLevel.NORMAL,
            alertType = null
        )
    }
}