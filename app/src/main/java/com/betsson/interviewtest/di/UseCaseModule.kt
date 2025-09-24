package com.betsson.interviewtest.di

import com.betsson.interviewtest.domain.repository.OddsRepository
import com.betsson.interviewtest.domain.usecase.GetSortedOddsStreamUseCase
import com.betsson.interviewtest.domain.usecase.TriggerOddsUpdateUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent // Or SingletonComponent if use cases are app-wide singletons
import dagger.hilt.android.scopes.ViewModelScoped // Or @Singleton
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
// Use cases are typically scoped to the ViewModel or are stateless singletons.
// If they are stateless and can be reused across the app, SingletonComponent is fine.
// If their lifecycle should be tied to ViewModels, use ViewModelComponent.
// For simplicity and common practice with use cases, let's assume they are stateless singletons.
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton // If use cases are stateless, Singleton is appropriate
    fun provideGetSortedOddsStreamUseCase(oddsRepository: OddsRepository): GetSortedOddsStreamUseCase {
        return GetSortedOddsStreamUseCase(oddsRepository)
    }

    @Provides
    @Singleton // If use cases are stateless, Singleton is appropriate
    fun provideTriggerOddsUpdateUseCase(oddsRepository: OddsRepository): TriggerOddsUpdateUseCase {
        return TriggerOddsUpdateUseCase(oddsRepository)
    }
}
