package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.model.MediaModel

interface PostRepository {
    val data: Flow<List<Post>>
    fun getNewerCount(latestId: Long): Flow<Int>
    suspend fun getAll()
    suspend fun showUnreadPosts()
    suspend fun saveWithAttachment(post: Post, media: MediaModel)
    suspend fun save(post: Post)
    suspend fun likeById(id: Long, idFromServer: Long, likedByMe: Boolean)
    suspend fun removeById(id: Long, idFromServer: Long)
    suspend fun viewById(id: Long)
    suspend fun getDraftCopy(): String
    suspend fun saveDraftCopy(content: String?)
    fun avatarUrl(authorAvatar: String): String
    fun attachmentUrl(url: String): String
}