package com.betsson.interviewtest.di

import com.betsson.interviewtest.data.logic.OddUpdateStrategyFactory
import com.betsson.interviewtest.data.logic.OddsLogicProcessor
import com.betsson.interviewtest.data.repository.OddsRepositoryImpl
import com.betsson.interviewtest.domain.repository.OddsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Scopes bindings to the application lifecycle
object AppModule { // Or use 'abstract class AppModule' if you have @Binds methods

    @Provides
    @Singleton
    fun provideOddUpdateStrategyFactory(): OddUpdateStrategyFactory {
        return OddUpdateStrategyFactory()
    }

    @Provides
    @Singleton // Ensures a single instance of OddsLogicProcessor throughout the app
    fun provideOddsLogicProcessor(
        strategyFactory: OddUpdateStrategyFactory
    ): OddsLogicProcessor {
        return OddsLogicProcessor(strategyFactory)
    }
    // If OddsRepositoryImpl takes OddsLogicProcessor in its constructor,
    // and OddsLogicProcessor is provided by Hilt (as above), Hilt can create OddsRepositoryImpl.
    // We then just need to tell Hilt what implementation to use for the OddsRepository interface.
}

// It's often cleaner to separate @Binds from @Provides if you have many of both.
// Or you can have them in the same module if it's small.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule { // Must be abstract for @Binds

    @Binds
    @Singleton // The scope here should generally match the scope of the concrete implementation
    abstract fun bindOddsRepository(
        oddsRepositoryImpl: OddsRepositoryImpl
    ): OddsRepository
}
