package ru.netology.nmedia.adapter

import ru.netology.nmedia.dto.Post

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onAttachments(post: Post)
    fun onView(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
}