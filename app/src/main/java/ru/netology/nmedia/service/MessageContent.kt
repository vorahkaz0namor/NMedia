package ru.netology.nmedia.service

data class MessageContent(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
    val postContent: String
)
