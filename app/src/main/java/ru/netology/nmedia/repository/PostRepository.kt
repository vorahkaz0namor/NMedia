package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(callback: PostCallback<List<Post>>)
    fun save(post: Post, callback: PostCallback<Post>)
    fun likeById(id: Long, likedByMe: Boolean, callback: PostCallback<Post>)
    fun removeById(id: Long, callback: PostCallback<Unit>)
    fun avatarUrl(authorAvatar: String): String
    fun attachmentUrl(url: String): String

    interface PostCallback<T> {
        fun onSuccess(result: T, code: Int)
        fun onError(code: Int)
    }
}