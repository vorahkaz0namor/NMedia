package ru.netology.nmedia.model

data class UiState(
    val id: Long = 0,
    val lastIdScrolled: Long = 0,
    val hasNotScrolledForCurrentId: Boolean = id != lastIdScrolled
) {
    override fun toString(): String =
        "id = $id\nlastIdScrolled = $lastIdScrolled\nhasNotScrolled... = $hasNotScrolledForCurrentId"
}