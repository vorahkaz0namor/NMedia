package ru.netology.nmedia.adapter

import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

class OnInteractionListenerImpl(
    private val viewModel: PostViewModel,
    private val authModel: AuthViewModel
) : OnInteractionListener, PathPointerImpl(viewModel) {
    override val authorized: Boolean
        get() = authModel.authorized

    override fun checkAuth() {
        authModel.checkAuth()
    }

    override fun onLike(post: Post) {
        viewModel.likeById(post)
    }

    override fun onShare(post: Post) {
        viewModel.shareById(post)
    }

    override fun onAttachments(post: Post) {
        viewModel.showAttachments(post)
    }

    override fun onEdit(post: Post) {
        viewModel.edit(post)
    }

    override fun repeatSave(post: Post) {
        viewModel.repeatSavePost(post)
    }

    override fun onRemove(post: Post) {
        viewModel.removeById(post.id, post.idFromServer)
    }

    override fun toSinglePost(post: Post) {
        viewModel.singlePost(post)
    }

    override fun avatarUrl(authorAvatar: String) =
        viewModel.getAvatarUrl(authorAvatar)
}