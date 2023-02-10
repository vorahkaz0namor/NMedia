package ru.netology.nmedia.adapter

import ru.netology.nmedia.dto.Post

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onAttachments(post: Post)
    fun onEdit(post: Post)
    fun repeatSave(post: Post)
    fun onRemove(post: Post)
    fun toSinglePost(post: Post)
    fun avatarUrl(authorAvatar: String): String
    fun attachmentUrl(url: String): String
}