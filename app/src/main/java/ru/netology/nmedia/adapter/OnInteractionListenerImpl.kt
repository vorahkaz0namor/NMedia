package ru.netology.nmedia.adapter

import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class OnInteractionListenerImpl(
    private val viewModel: PostViewModel
) : OnInteractionListener {
    override fun onLike(post: Post) {
        viewModel.likeById(post.id)
    }

    override fun onShare(post: Post) {
        viewModel.shareById(post)
    }

    override fun onAttachments(post: Post) {
        viewModel.showAttachments(post)
    }

    override fun onView(post: Post) {
        viewModel.viewById(post.id)
    }

    override fun onEdit(post: Post) {
        viewModel.edit(post)
    }

    override fun onRemove(post: Post) {
        viewModel.removeById(post.id)
    }
}