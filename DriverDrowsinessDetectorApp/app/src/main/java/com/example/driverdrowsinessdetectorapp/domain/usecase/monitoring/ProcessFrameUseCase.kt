package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring

import android.graphics.Bitmap
import android.util.Log
import com.example.driverdrowsinessdetectorapp.domain.model.MetricasSomnolencia
import com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.extraction.ExtractLandmarksUseCase
import javax.inject.Inject

class ProcessFrameUseCase @Inject constructor(
    private val extractLandmarksUseCase: ExtractLandmarksUseCase,
    private val detectDrowsinessUseCase: DetectDrowsinessUseCase
) {
    companion object {
        private const val TAG = "ProcessFrameUseCase"
    }
    
    operator fun invoke(bitmap: Bitmap): MetricasSomnolencia? {
        return try {
            val landmarksResult = extractLandmarksUseCase(bitmap)
            
            //  SIEMPRE procesar, incluso sin rostro (para mantener estado de cabeceo)
            val metrics = detectDrowsinessUseCase(
                faceLandmarks = landmarksResult.faceLandmarks,  
                handLandmarks = landmarksResult.handLandmarks,
                handedness = landmarksResult.handedness
            )
            
            if (!landmarksResult.hasFace) {
                Log.d(TAG, "⚠️ Frame sin rostro - Procesando cabeceo: isNodding=${metrics.isNodding}")
            }
            
            metrics
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al procesar frame: ${e.message}", e)
            null
        }
    }
}