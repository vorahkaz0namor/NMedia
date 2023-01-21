package ru.netology.nmedia.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//@Parcelize
data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String = "",
    val published: Long,
    val content: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val views: Int = 0,
    val attachment: Attachment? = null
)/*: Parcelable*/