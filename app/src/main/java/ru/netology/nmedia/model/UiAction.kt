package ru.netology.nmedia.model

sealed class UiAction {
    data class Get(val id: Long) : UiAction()
    data class Scroll(val currentId: Long) : UiAction()
}
