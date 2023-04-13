package ru.netology.nmedia.adapter

import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.dto.CountDisplay.daySeparator
import ru.netology.nmedia.util.CompanionNotMedia.actualTime
import ru.netology.nmedia.util.CompanionNotMedia.load

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(previousPost: Post?, currentPost: Post) {
        fillingCardPost(previousPost = previousPost, currentPost = currentPost)
        setupListeners(currentPost)
    }

    private fun fillingCardPost(previousPost: Post?, currentPost: Post) {
        binding.apply {
            daySeparator.apply {
                when (val sep = daySeparator(
                                    previousPost = previousPost,
                                    currentPost = currentPost
                                )) {
                    null -> isVisible = false
                    else -> {
                        isVisible = true
                        text = sep
                    }
                }
            }
            author.text = currentPost.author
            published.text = actualTime(currentPost.published)
            content.text = currentPost.content
            avatar.apply {
                if (currentPost.authorAvatar == "localuser.jpg" ||
                    currentPost.authorAvatar == "")
                    setImageResource(R.drawable.ic_local_user_24)
                else
                    load(onInteractionListener.avatarUrl(currentPost.authorAvatar))
            }
            menu.isVisible = currentPost.ownedByMe
            deprecatedActions.apply {
                if (currentPost.isOnServer) {
                    isVisible = true
                    repeatSavePost.isVisible = false
                } else {
                    isVisible = false
                    repeatSavePost.isVisible = true
                }
            }
            postAttachment.apply {
                if (currentPost.attachment != null && currentPost.isOnServer) {
                    isVisible = true
                    contentDescription = currentPost.attachment.description
                    load(
                        onInteractionListener.attachmentUrl(currentPost.attachment.url),
                        currentPost.attachment.type.name
                    )
                } else
                    isVisible = false
            }
            likes.isChecked = currentPost.likedByMe
            likes.text = CountDisplay.show(currentPost.likes)
            share.text = CountDisplay.show(currentPost.shares)
            views.text = CountDisplay.show(currentPost.views)
        }
    }

    private fun setupListeners(currentPost: Post) {
        binding.apply {
            root.setOnClickListener {
                onInteractionListener.checkAuth()
                if (onInteractionListener.authorized)
                    onInteractionListener.toSinglePost(currentPost)
            }
            likes.setOnClickListener {
                onInteractionListener.checkAuth()
                if (onInteractionListener.authorized)
                    onInteractionListener.onLike(currentPost)
                else
                    likes.isChecked = false
            }
            // Click Share
            share.setOnClickListener {
                onInteractionListener.checkAuth()
                if (onInteractionListener.authorized)
                    onInteractionListener.onShare(currentPost)
            }
            postAttachment.setOnClickListener {
                onInteractionListener.checkAuth()
                if (onInteractionListener.authorized)
                    onInteractionListener.onAttachments(currentPost)
            }
            repeatSavePost.setOnClickListener {
                onInteractionListener.repeatSave(currentPost)
            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            // Click Remove
                            R.id.remove -> {
                                onInteractionListener.onRemove(currentPost)
                                true
                            }
                            // Click Edit
                            R.id.edit -> {
                                onInteractionListener.onEdit(currentPost)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}