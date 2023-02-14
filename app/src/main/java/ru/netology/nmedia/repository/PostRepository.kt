package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    fun getNewerCount(latestId: Long): Flow<Int>
    suspend fun getAll()
    suspend fun showUnreadPosts()
    suspend fun save(post: Post)
    suspend fun likeById(id: Long, idFromServer: Long, likedByMe: Boolean): Post
    suspend fun removeById(id: Long, idFromServer: Long)
    suspend fun viewById(id: Long)
    fun avatarUrl(authorAvatar: String): String
    fun attachmentUrl(url: String): String
}