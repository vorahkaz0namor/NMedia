package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val loading: Boolean = false,
    val error: Boolean = false,
    val empty: Boolean = false,
    val showing: Boolean = false
) {
    fun loading() =
        this.copy(
            loading = true,
            showing = false,
            error = false
        )

    fun showing(posts: List<Post>) =
        this.copy(
            loading = false,
            showing = true,
                posts = posts,
                empty = posts.isEmpty(),
            error = false
        )

    fun error() =
        this.copy(
            loading = false,
            showing = false,
            error = true
        )
}