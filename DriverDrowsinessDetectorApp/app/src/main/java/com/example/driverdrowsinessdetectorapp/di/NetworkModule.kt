package com.example.driverdrowsinessdetectorapp.di

import com.example.driverdrowsinessdetectorapp.data.local.preferences.PreferencesManager
import com.example.driverdrowsinessdetectorapp.data.remote.api.AlertApi
import com.example.driverdrowsinessdetectorapp.data.remote.api.AuthApi
import com.example.driverdrowsinessdetectorapp.data.remote.interceptor.AuthInterceptor
import com.example.driverdrowsinessdetectorapp.data.remote.interceptor.LoggingInterceptor
import com.example.driverdrowsinessdetectorapp.data.repository.AlertRepositoryImpl
import com.example.driverdrowsinessdetectorapp.data.repository.AuthRepositoryImpl
import com.example.driverdrowsinessdetectorapp.domain.repository.AlertRepository
import com.example.driverdrowsinessdetectorapp.domain.repository.AuthRepository
import com.example.driverdrowsinessdetectorapp.util.Constants
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        preferencesManager: PreferencesManager
    ): AuthInterceptor {
        return AuthInterceptor(preferencesManager)
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        // Usar el método create() del object LoggingInterceptor
        return LoggingInterceptor.create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAlertApi(retrofit: Retrofit): AlertApi {
        return retrofit.create(AlertApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        preferencesManager: PreferencesManager
    ): AuthRepository {
        return AuthRepositoryImpl(authApi, preferencesManager)
    }

    @Provides
    @Singleton
    fun provideAlertRepository(
        alertApi: AlertApi
    ): AlertRepository {
        return AlertRepositoryImpl(alertApi)
    }
}

