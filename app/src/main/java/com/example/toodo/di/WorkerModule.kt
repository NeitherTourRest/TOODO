package com.example.toodo.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    // Worker dependencies are provided through HiltWorkerFactory
    // Workers use @AssistedInject for their dependencies
}
