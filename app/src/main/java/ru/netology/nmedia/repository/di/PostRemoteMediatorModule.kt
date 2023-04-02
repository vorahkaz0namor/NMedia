package ru.netology.nmedia.repository.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRemoteMediator
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class PostRemoteMediatorModule {
    @Singleton
    @Provides
    fun providePostRemoteMediator(
        postApiService: PostApiService,
        postDao: PostDao,
        postRemoteKeyDao: PostRemoteKeyDao,
        appDb: AppDb
    ): PostRemoteMediator = PostRemoteMediator(
        postApiService = postApiService,
        postDao = postDao,
        postRemoteKeyDao = postRemoteKeyDao,
        appDb = appDb
    )
}