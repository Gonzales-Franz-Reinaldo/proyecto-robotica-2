package com.example.driverdrowsinessdetectorapp.data.mediapipe

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper para MediaPipe Face Landmarker
 * 
 * Equivalente a: drowsiness_processor/extract_points/face_mesh/face_mesh_processor.py
 * 
 * Detecta 468 puntos faciales (ojos, boca, contorno facial, etc.)
 */
@Singleton
class FaceLandmarkerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "FaceLandmarkerHelper"
        private const val MODEL_ASSET = "face_landmarker.task"
        private const val MIN_DETECTION_CONFIDENCE = 0.5f
        private const val MIN_PRESENCE_CONFIDENCE = 0.5f
        private const val MIN_TRACKING_CONFIDENCE = 0.5f
    }

    private var faceLandmarker: FaceLandmarker? = null
    private var isInitialized = false

    init {
        initializeLandmarker()
    }

    /**
     * Inicializar MediaPipe Face Landmarker
     */
    private fun initializeLandmarker() {
        try {
            // VERIFICAR QUE EL ARCHIVO EXISTE
            val assetManager = context.assets
            val inputStream = assetManager.open(MODEL_ASSET)
            val fileSize = inputStream.available()
            inputStream.close()
            
            Log.d(TAG, "✅ Archivo encontrado: $MODEL_ASSET ($fileSize bytes)")
            
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET)
                .build()

            val options = FaceLandmarker.FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setNumFaces(1)
                .setMinFaceDetectionConfidence(MIN_DETECTION_CONFIDENCE)
                .setMinFacePresenceConfidence(MIN_PRESENCE_CONFIDENCE)
                .setMinTrackingConfidence(MIN_TRACKING_CONFIDENCE)
                .setRunningMode(RunningMode.IMAGE)
                .build()

            faceLandmarker = FaceLandmarker.createFromOptions(context, options)
            isInitialized = true
            Log.d(TAG, "✅ FaceLandmarker inicializado correctamente")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al inicializar FaceLandmarker: ${e.message}", e)
            isInitialized = false
        }
    }
    
    /**
     * Detectar landmarks faciales en un bitmap
     * 
     * @param bitmap Imagen a procesar
     * @return FaceLandmarkerResult con 468 puntos faciales (si detecta rostro)
     */
    fun detect(bitmap: Bitmap): FaceLandmarkerResult? {
        if (!isInitialized || faceLandmarker == null) {
            Log.w(TAG, "⚠️ FaceLandmarker no está inicializado")
            return null
        }

        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = faceLandmarker?.detect(mpImage)
            
            if (result?.faceLandmarks()?.isEmpty() == true) {
                Log.w(TAG, "⚠️ No se detectó rostro en el frame")
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al detectar landmarks: ${e.message}", e)
            null
        }
    }

    /**
     * Limpiar recursos
     */
    fun close() {
        faceLandmarker?.close()
        faceLandmarker = null
        isInitialized = false
        Log.d(TAG, "🧹 FaceLandmarker cerrado")
    }
}