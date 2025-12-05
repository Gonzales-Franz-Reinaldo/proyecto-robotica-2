package com.example.driverdrowsinessdetectorapp.di

import android.content.Context
import com.example.driverdrowsinessdetectorapp.data.mediapipe.FaceLandmarkerHelper
import com.example.driverdrowsinessdetectorapp.data.mediapipe.HandLandmarkerHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaPipeModule {

    @Provides
    @Singleton
    fun provideFaceLandmarkerHelper(
        @ApplicationContext context: Context
    ): FaceLandmarkerHelper {
        return FaceLandmarkerHelper(context)
    }

    @Provides
    @Singleton
    fun provideHandLandmarkerHelper(
        @ApplicationContext context: Context
    ): HandLandmarkerHelper {
        return HandLandmarkerHelper(context)
    }
}