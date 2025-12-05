package com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.extraction

import android.graphics.Bitmap
import android.util.Log
import com.example.driverdrowsinessdetectorapp.data.mediapipe.FaceLandmarkerHelper
import com.example.driverdrowsinessdetectorapp.data.mediapipe.HandLandmarkerHelper
import com.google.mediapipe.tasks.components.containers.Category  
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import javax.inject.Inject

data class LandmarksResult(
    val faceLandmarks: List<NormalizedLandmark>?,
    val handLandmarks: List<List<NormalizedLandmark>>?,
    val handedness: List<List<Category>>?, 
    val hasFace: Boolean,
    val hasHands: Boolean
)

class ExtractLandmarksUseCase @Inject constructor(
    private val faceLandmarkerHelper: FaceLandmarkerHelper,
    private val handLandmarkerHelper: HandLandmarkerHelper
) {
    companion object {
        private const val TAG = "ExtractLandmarksUseCase"
    }
    
    operator fun invoke(bitmap: Bitmap): LandmarksResult {
        val faceResult = faceLandmarkerHelper.detect(bitmap)
        val faceLandmarks = faceResult?.faceLandmarks()?.firstOrNull()
        
        val handResult = handLandmarkerHelper.detect(bitmap)
        val handLandmarks = handResult?.landmarks()
        val handedness = handResult?.handednesses()
        
        val hasFace = faceLandmarks != null && faceLandmarks.isNotEmpty()
        val hasHands = handLandmarks?.isNotEmpty() == true
        
        if (!hasFace) {
            Log.w(TAG, "⚠️ No se detectó rostro en el frame")
        }
        
        return LandmarksResult(
            faceLandmarks = faceLandmarks,
            handLandmarks = handLandmarks,
            handedness = handedness,
            hasFace = hasFace,
            hasHands = hasHands
        )
    }
}