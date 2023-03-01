package ru.netology.nmedia.model

data class AuthModelState(
    val loading: Boolean = false,
    val showing: Boolean = true
) {
    fun loading() =
        this.copy(
            loading = true,
            showing = false
        )

    fun showing() =
        this.copy(
            loading = false,
            showing = true
        )
}