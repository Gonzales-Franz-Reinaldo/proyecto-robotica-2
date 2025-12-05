package com.example.driverdrowsinessdetectorapp.data.mediapipe

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HandLandmarkerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "HandLandmarkerHelper"
        private const val MODEL_ASSET = "hand_landmarker.task"
        private const val MIN_DETECTION_CONFIDENCE = 0.5f
        private const val MIN_PRESENCE_CONFIDENCE = 0.5f
        private const val MIN_TRACKING_CONFIDENCE = 0.5f
    }

    private var handLandmarker: HandLandmarker? = null
    private var isInitialized = false

    init {
        initializeLandmarker()
    }

    private fun initializeLandmarker() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET)
                .build()

            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setNumHands(2)
                .setMinHandDetectionConfidence(MIN_DETECTION_CONFIDENCE)
                .setMinHandPresenceConfidence(MIN_PRESENCE_CONFIDENCE)
                .setMinTrackingConfidence(MIN_TRACKING_CONFIDENCE)
                .setRunningMode(RunningMode.IMAGE)
                .build()

            handLandmarker = HandLandmarker.createFromOptions(context, options)
            isInitialized = true
            Log.d(TAG, "✅ HandLandmarker inicializado correctamente")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al inicializar HandLandmarker: ${e.message}", e)
            isInitialized = false
        }
    }

    /**
     * Detectar landmarks de manos en un bitmap
     */
    fun detect(bitmap: Bitmap): HandLandmarkerResult? {
        if (!isInitialized || handLandmarker == null) {
            Log.w(TAG, "⚠️ HandLandmarker no está inicializado")
            return null
        }

        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            handLandmarker?.detect(mpImage)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al detectar manos: ${e.message}", e)
            null
        }
    }

    /**
     * Limpiar recursos
     */
    fun close() {
        handLandmarker?.close()
        handLandmarker = null
        isInitialized = false
        Log.d(TAG, "🧹 HandLandmarker cerrado")
    }
}