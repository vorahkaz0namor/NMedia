package ru.netology.nmedia.dto

// Данный интерфейс объединяет классы Post и Ad.
// Чтобы ограничить количество реализаций этого интерфейса,
// надо использовать аннотацию sealed
sealed interface FeedItem {
    val id: Long
}