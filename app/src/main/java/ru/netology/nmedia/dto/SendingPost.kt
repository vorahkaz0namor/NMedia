package ru.netology.nmedia.dto

data class SendingPost(
    val id: Long,
    val author: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0
) {

    companion object {
        fun fromDto(dtoPost: Post) =
            SendingPost(
                dtoPost.id,
                dtoPost.author,
                dtoPost.content,
                dtoPost.published,
                dtoPost.likedByMe,
                dtoPost.likes
            )
    }
}