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
        this.copy(loading = true, error = false, showing = false)

    fun showing(posts: List<Post>) =
        this.copy(loading = false, posts = posts, empty = posts.isEmpty(), showing = true)

    fun error() =
        this.copy(loading = false, error = true)
}