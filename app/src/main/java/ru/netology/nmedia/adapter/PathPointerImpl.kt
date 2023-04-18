package ru.netology.nmedia.adapter

import ru.netology.nmedia.viewmodel.PostViewModel

open class PathPointerImpl(
    private val viewModel: PostViewModel
) : PathPointer {
    override fun attachmentUrl(url: String): String =
        viewModel.getAttachmentUrl(url)
}