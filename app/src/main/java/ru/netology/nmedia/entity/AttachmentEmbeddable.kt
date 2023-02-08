package ru.netology.nmedia.entity

import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.enum.AttachmentType

//@Embeddable - Unresolved reference
data class AttachmentEmbeddable(
    val url: String,
    val description: String?,
    val type: AttachmentType
) {
    fun toDto() =
        Attachment(
            url = url,
            description = description,
            type = type
        )

    companion object {
        fun fromDto(dto: Attachment?) =
            dto?.let {
                AttachmentEmbeddable(
                    url = it.url,
                    description = it.description,
                    type = it.type
                )
            }
    }
}