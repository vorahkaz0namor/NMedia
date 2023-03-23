package ru.netology.nmedia.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.repository.AuthRepository
import ru.netology.nmedia.repository.AuthRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface AuthRepositoryModule {
    @Singleton
    @Binds
    fun bindsAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}