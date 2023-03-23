package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DraftCopyEntity(
    @PrimaryKey
    val content: String
) {
    companion object {
        fun fromDto(draftCopy: String?) = DraftCopyEntity(draftCopy ?: "")
    }
}