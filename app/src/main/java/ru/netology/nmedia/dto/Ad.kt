package ru.netology.nmedia.dto

import ru.netology.nmedia.util.CompanionNotMedia.Type

data class Ad(
    override val id: Long,
    val image: String,
    val type: String = Type.IMAGE.name
) : FeedItem
