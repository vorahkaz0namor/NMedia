package ru.netology.nmedia.dao

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import ru.netology.nmedia.entity.PostEntity
import java.util.*

@Dao
interface PostDao {
    // То, что возвращает "пописку" (LiveData) можно не оборачивать
    // в suspend, ибо это не такое уж "тяжелое" действие
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Query("SELECT content FROM DraftCopyEntity")
    fun cursor(): Cursor

    @Query("UPDATE DraftCopyEntity SET content = :content")
    suspend fun updateDraftCopy(content: String?)

    @Query("INSERT INTO DraftCopyEntity (content) VALUES (:content)")
    suspend fun insertDraftCopy(content: String?)

    suspend fun getDraftCopy(): String? {
        cursor().apply {
            return if (this.moveToFirst())
                this.getString(
                    this.getColumnIndexOrThrow(
                        this.columnNames.first()
                    )
                )
            else
                null
        }
    }

    suspend fun saveDraftCopy(content: String?) {
        cursor().apply {
            if (this.moveToFirst())
                updateDraftCopy(content)
            else
                insertDraftCopy(content)
        }
    }

    @Insert(onConflict = REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content, published = :published WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String, published: Long)

    suspend fun save(post: PostEntity) =
        if (post.id == 0L)
            insert(post)
        else {
            updateContentById(post.id, post.content, post.published)
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

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)
}
