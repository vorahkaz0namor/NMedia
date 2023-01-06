package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val published: Long,
    val content: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val views: Int = 0,
    val attachments: String? = null
) {
    fun toDto() =
        Post(
            id,
            author,
            published,
            content,
            likes,
            likedByMe,
            shares,
            views,
            attachments
        )

    companion object {
        fun fromDto(dtoPost: Post) =
            PostEntity(
                dtoPost.id,
                dtoPost.author,
                dtoPost.published,
                dtoPost.content,
                dtoPost.likes,
                dtoPost.likedByMe,
                dtoPost.shares,
                dtoPost.views,
                dtoPost.attachments
            )
    }
}