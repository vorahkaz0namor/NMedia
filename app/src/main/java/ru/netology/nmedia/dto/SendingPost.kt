package ru.netology.nmedia.dto

data class SendingPost(
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0
) {

    companion object {
        fun fromDto(dtoPost: Post) =
            SendingPost(
                id = dtoPost.id,
                author = dtoPost.author,
                authorAvatar = dtoPost.authorAvatar,
                content = dtoPost.content,
                published = dtoPost.published,
                likedByMe = dtoPost.likedByMe,
                likes = dtoPost.likes
            )
    }
}