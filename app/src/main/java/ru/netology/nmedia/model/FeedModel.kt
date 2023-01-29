package ru.netology.nmedia.model

import okhttp3.internal.http.HTTP_CONTINUE
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.CompanionNotMedia.overview

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val code: Int = HTTP_CONTINUE,
    val loading: Boolean = false,
    val error: Boolean = false,
    val empty: Boolean = false,
    val showing: Boolean = false
) {
    val codeOverview = overview(code)

    fun loading() =
        this.copy(
            loading = true,
            showing = false,
            error = false
        )

    fun showing(posts: List<Post>, code: Int) =
        this.copy(
            loading = false,
            showing = true,
                posts = posts,
                empty = posts.isEmpty(),
            error = false,
                code = code
        )

    fun error(code: Int) =
        this.copy(
            loading = false,
            showing = false,
            error = true,
                code = code
        )
}