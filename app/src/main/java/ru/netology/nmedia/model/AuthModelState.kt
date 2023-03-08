package ru.netology.nmedia.model

data class AuthModelState(
    val loading: Boolean = false,
    val authShowing: Boolean = true,
    val regShowing: Boolean = false
) {
    fun loading() =
        this.copy(
            loading = true,
            authShowing = false,
            regShowing = false
        )

    fun authShowing() =
        this.copy(
            loading = false,
            authShowing = true,
            regShowing = false
        )

    fun regShowing() =
        this.copy(
            loading = false,
            authShowing = false,
            regShowing = true
        )

    fun error() =
        this.copy(
            loading = false,
            authShowing = false,
            regShowing = false
        )
}