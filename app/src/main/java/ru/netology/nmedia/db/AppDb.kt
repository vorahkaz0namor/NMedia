package ru.netology.nmedia.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.DraftCopyEntity
import ru.netology.nmedia.entity.PostEntity

@Database(
    entities = [
        PostEntity::class,
        DraftCopyEntity::class
    ],
    version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
}