package ru.netology.nmedia.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface PostRepositoryModule {
    // Для связывания интерфейса репозитория и его реализации
    // используется аннотация @Binds
    @Singleton
    @Binds
    fun bindsPostRepository(
        impl: PostRepositoryImpl
    ): PostRepository
}