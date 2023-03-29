package ru.netology.nmedia.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Чтобы модуль использовался (зависимость была предоставлена)
// на уровне всего приложения, ставится аннотация @InstallIn()
@InstallIn(SingletonComponent::class)
@Module
class DbModule {
    // Чтобы объект существовал в рамках всего приложения
    // в одном единственном экземпляре, то аннотация:
    @Singleton
    // Для создания вручную ставится аннотация @Provides
    @Provides
    fun provideDb(
        // Context сюда предоставляет сама библиотека Hilt
        @ApplicationContext
        context: Context
    ): AppDb = Room.databaseBuilder(
        context,
        AppDb::class.java,
        "nmedia.db"
    )
        .build()

    // Перенесено в DaoModule
//    @Provides
//    fun providePostDao(
//        appDb: AppDb
//    ): PostDao = appDb.postDao()
}