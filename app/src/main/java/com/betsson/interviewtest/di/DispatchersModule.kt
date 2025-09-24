package com.betsson.interviewtest.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

// Qualifiers to distinguish between different dispatchers
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    @DefaultDispatcher // Custom qualifier
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @IoDispatcher // Custom qualifier
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    // You could also provide Dispatchers.Main if needed, though it's often accessed directly
    // or through MainCoroutineRule in tests.
}