package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DraftCopyEntity(
    @PrimaryKey
    val id: Int,
    val content: String? = null
)