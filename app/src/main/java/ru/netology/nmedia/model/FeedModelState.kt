package ru.netology.nmedia.model

import okhttp3.internal.http.HTTP_CONTINUE
import retrofit2.HttpException
import ru.netology.nmedia.util.CompanionNotMedia.overview

data class FeedModelState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val showing: Boolean = false,
    val error: Boolean = false
) {
    fun loading() =
        this.copy(
            loading = true,
            refreshing = false,
            showing = false,
            error = false
        )

    fun refreshing() =
        this.copy(
            loading = true,
            refreshing = true,
            showing = false,
            error = false
        )

    fun showing() =
        this.copy(
            loading = false,
            refreshing = false,
            showing = true,
            error = false
        )

    fun error() =
        this.copy(
            loading = false,
            refreshing = false,
            showing = false,
            error = true
        )
}
