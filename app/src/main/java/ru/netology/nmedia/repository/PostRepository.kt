package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: LiveData<List<Post>>
    suspend fun getAll()
    suspend fun save(post: Post): Post
    suspend fun likeById(id: Long, likedByMe: Boolean): Post
    suspend fun removeById(id: Long)
    suspend fun viewById(id: Long)
    fun avatarUrl(authorAvatar: String): String
    fun attachmentUrl(url: String): String
}