package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(callback: PostCallback<List<Post>>)
    fun save(post: Post, callback: PostCallback<Int>)
    fun likeById(id: Long, likedByMe: Boolean, callback: PostCallback<Post>)
    fun removeById(id: Long, callback: PostCallback<Int>)
    fun avatarUrl(authorAvatar: String): String

    interface PostCallback<T> {
        fun onSuccess(result: T)
        fun onError(e: Exception)
    }
}