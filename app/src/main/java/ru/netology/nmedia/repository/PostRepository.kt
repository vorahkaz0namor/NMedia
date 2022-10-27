package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): LiveData<List<Post>>
    fun save(post: Post)
    fun likeById(id: Long): Boolean
    fun shareById(id: Long): Boolean
    fun viewById(id: Long): Boolean
    fun removeById(id: Long)
}