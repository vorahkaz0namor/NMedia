package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import ru.netology.nmedia.entity.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {
    @Query("SELECT max(`key`) FROM PostRemoteKeyEntity")
    suspend fun max(): Long?

    @Query("SELECT min(`key`) FROM PostRemoteKeyEntity")
    suspend fun min(): Long?

    @Query("SELECT max(`key`) FROM PostRemoteKeyEntity")
    fun after(): Long?

    @Query("SELECT min(`key`) FROM PostRemoteKeyEntity")
    fun before(): Long?

    @Insert(onConflict = REPLACE)
    suspend fun saveRemoteKey(postRemoteKeyEntity: PostRemoteKeyEntity)

    @Insert(onConflict = REPLACE)
    suspend fun saveRemoteKey(listPostRemoteKeyEntity: List<PostRemoteKeyEntity>)

    @Query("DELETE FROM PostRemoteKeyEntity")
    suspend fun clearRemoteKey()
}