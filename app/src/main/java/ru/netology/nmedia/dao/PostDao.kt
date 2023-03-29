package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import ru.netology.nmedia.entity.DraftCopyEntity
import ru.netology.nmedia.entity.PostEntity
import java.util.*

@Dao
interface PostDao {
    // То, что возвращает "подписку" (LiveData) можно не оборачивать
    // в suspend, ибо это не такое уж "тяжелое" действие.
    // В проекте https://github.com/android/architecture-components-samples/tree/main/PagingSample
    // аналогичная функция также не является suspend
    @Query("SELECT * FROM PostEntity WHERE hidden = 0 ORDER BY id DESC")
    fun getAllRead(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    suspend fun getAll(): List<PostEntity>

    @Query("SELECT id FROM PostEntity WHERE hidden = 1")
    suspend fun getUnread(): List<Long>

    @Query("SELECT MAX(id) FROM PostEntity")
    suspend fun getInsertedPostId(): Long

    @Insert
    suspend fun insert(post: PostEntity)

    @Query("""
        UPDATE PostEntity SET 
        idFromServer = :idFromServer,
        content = :content, 
        published = :published 
        WHERE id = :id
        """)
    suspend fun updateContentById(id: Long, idFromServer: Long, content: String, published: Long)

    @Insert(onConflict = REPLACE)
    suspend fun updatePostsByIdFromServer(post: List<PostEntity>)

    suspend fun save(post: PostEntity) =
        if (post.id == 0L) {
            insert(post)
            getInsertedPostId()
        }
        else {
//            updateContentById(post.id, post.idFromServer, post.content, post.published)
            updatePostsByIdFromServer(listOf(post))
            post.id
        }

    @Query("""
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
    """)
    suspend fun likeById(id: Long)

    @Query("UPDATE PostEntity SET shares = shares + 1 WHERE id = :id")
    suspend fun shareById(id: Long)

    @Query("UPDATE PostEntity SET views = views + 1 WHERE id = :id")
    suspend fun viewById(id: Long)

    @Query("""UPDATE PostEntity SET 
        hidden = CASE WHEN hidden THEN 0 END 
        WHERE id = :id""")
    suspend fun updateHiddenToFalse(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM PostEntity")
    suspend fun removeAllPosts()

    // Block to work with DraftCopy

    @Query("SELECT content FROM DraftCopyEntity")
    suspend fun getDraftCopy(): String

    @Query("DELETE FROM DraftCopyEntity")
    suspend fun clearDraftCopy()

    @Insert(onConflict = REPLACE)
    suspend fun saveDraftCopy(draftCopy: DraftCopyEntity)
}
