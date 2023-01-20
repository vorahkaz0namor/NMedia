package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String = "",
    val published: Long,
    val content: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val views: Int = 0,
    val attachment: String? = null
) {
    fun toDto() =
        Post(
            id = id,
            author = author,
            authorAvatar = authorAvatar,
            published = published,
            content = content,
            likes = likes,
            likedByMe = likedByMe,
            shares = shares,
            views = views,
        )

    companion object {
        fun fromDto(dtoPost: Post) =
            PostEntity(
                id = dtoPost.id,
                author = dtoPost.author,
                authorAvatar = dtoPost.authorAvatar,
                published = dtoPost.published,
                content = dtoPost.content,
                likes = dtoPost.likes,
                likedByMe = dtoPost.likedByMe,
                shares = dtoPost.shares,
                views = dtoPost.views,
                attachment = dtoPost.attachment?.url
            )
    }
}