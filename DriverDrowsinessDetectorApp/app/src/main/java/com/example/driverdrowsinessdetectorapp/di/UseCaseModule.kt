package com.example.driverdrowsinessdetectorapp.di

import com.example.driverdrowsinessdetectorapp.data.mediapipe.FaceLandmarkerHelper
import com.example.driverdrowsinessdetectorapp.data.mediapipe.HandLandmarkerHelper
import com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.*
import com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.extraction.ExtractLandmarksUseCase
import com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.features.*
import com.example.driverdrowsinessdetectorapp.domain.usecase.monitoring.processing.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    // ========================================
    //  PROCESSING USE CASES
    // ========================================
    
    @Provides
    @Singleton
    fun provideCalculateEyeDistancesUseCase(): CalculateEyeDistancesUseCase {
        return CalculateEyeDistancesUseCase()
    }

    @Provides
    @Singleton
    fun provideCalculateMouthDistancesUseCase(): CalculateMouthDistancesUseCase {
        return CalculateMouthDistancesUseCase()
    }

    @Provides
    @Singleton
    fun provideDetectHeadPositionUseCase(): DetectHeadPositionUseCase {
        return DetectHeadPositionUseCase()
    }

    @Provides
    @Singleton
    fun provideCalculateEARUseCase(): CalculateEARUseCase {
        return CalculateEARUseCase()
    }
    
    @Provides
    @Singleton
    fun provideCalculateMARUseCase(): CalculateMARUseCase {
        return CalculateMARUseCase()
    }
    
    @Provides
    @Singleton
    fun provideDetectHeadPoseUseCase(): DetectHeadPoseUseCase {
        return DetectHeadPoseUseCase()
    }
    
    @Provides
    @Singleton
    fun provideDetectHandNearEyesUseCase(): DetectHandNearEyesUseCase {
        return DetectHandNearEyesUseCase()
    }
    
    // ========================================
    // EXTRACTION USE CASES
    // ========================================
    
    @Provides
    @Singleton
    fun provideExtractLandmarksUseCase(
        faceLandmarkerHelper: FaceLandmarkerHelper,
        handLandmarkerHelper: HandLandmarkerHelper
    ): ExtractLandmarksUseCase {
        return ExtractLandmarksUseCase(faceLandmarkerHelper, handLandmarkerHelper)
    }
    
    // ========================================
    // FEATURES USE CASES (DETECCIÓN)
    // ========================================
    
    @Provides
    @Singleton
    fun provideDetectBlinkUseCase(): DetectBlinkUseCase {
        return DetectBlinkUseCase() 
    }
    
    @Provides
    @Singleton
    fun provideDetectMicrosleepUseCase(): DetectMicrosleepUseCase {
        return DetectMicrosleepUseCase()
    }
    
    @Provides
    @Singleton
    fun provideDetectYawnUseCase(): DetectYawnUseCase {
        return DetectYawnUseCase() 
    }
    
    @Provides
    @Singleton
    fun provideDetectNoddingUseCase(): DetectNoddingUseCase {
        return DetectNoddingUseCase()
    }
    
    @Provides
    @Singleton
    fun provideDetectEyeRubUseCase(): DetectEyeRubUseCase {
        return DetectEyeRubUseCase() 
    }
    
    // ========================================
    // MAIN ORCHESTRATORS
    // ========================================
    
    @Provides
    @Singleton
    fun provideDetectDrowsinessUseCase(
        calculateEyeDistancesUseCase: CalculateEyeDistancesUseCase,
        calculateMouthDistancesUseCase: CalculateMouthDistancesUseCase,
        calculateMARUseCase: CalculateMARUseCase,
        detectHeadPositionUseCase: DetectHeadPositionUseCase,
        detectHandNearEyesUseCase: DetectHandNearEyesUseCase,
        detectBlinkUseCase: DetectBlinkUseCase,
        detectMicrosleepUseCase: DetectMicrosleepUseCase,
        detectYawnUseCase: DetectYawnUseCase,
        detectNoddingUseCase: DetectNoddingUseCase,
        detectEyeRubUseCase: DetectEyeRubUseCase
    ): DetectDrowsinessUseCase {
        return DetectDrowsinessUseCase(
            calculateEyeDistancesUseCase,
            calculateMouthDistancesUseCase,
            calculateMARUseCase,
            detectHeadPositionUseCase,
            detectHandNearEyesUseCase,
            detectBlinkUseCase,
            detectMicrosleepUseCase,
            detectYawnUseCase,
            detectNoddingUseCase,
            detectEyeRubUseCase
        )
    }
    
    @Provides
    @Singleton
    fun provideProcessFrameUseCase(
        extractLandmarksUseCase: ExtractLandmarksUseCase,
        detectDrowsinessUseCase: DetectDrowsinessUseCase
    ): ProcessFrameUseCase {
        return ProcessFrameUseCase(extractLandmarksUseCase, detectDrowsinessUseCase)
    }
}