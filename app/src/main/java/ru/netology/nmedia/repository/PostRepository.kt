package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun save(post: Post)
    fun likeById(id: Long, likedByMe: Boolean): Post
    fun viewById(id: Long)
    fun removeById(id: Long)
}