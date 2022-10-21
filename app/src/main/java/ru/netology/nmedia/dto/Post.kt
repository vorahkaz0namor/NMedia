package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val published: String,
    val content: String,
    val likes: Int,
    val likedByMe: Boolean = false,
    val shares: Int,
    val views: Int
)
