package ru.netology.nmedia.adapter

import android.view.PointerIcon
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.AppActivity
import ru.netology.nmedia.application.NMediaApp
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.util.CompanionNotMedia.actualTime
import ru.netology.nmedia.util.CompanionNotMedia.load

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        fillingCardPost(post)
        setupListeners(post)
    }

    private fun fillingCardPost(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = actualTime(post.published)
            content.text = post.content
            avatar.apply {
                if (post.authorAvatar == "localuser.jpg")
                    setImageResource(R.drawable.ic_local_user_24)
                else
                    load(onInteractionListener.avatarUrl(post.authorAvatar))
            }
            menu.isVisible = post.ownedByMe
            deprecatedActions.apply {
                if (post.isOnServer) {
                    isVisible = true
                    repeatSavePost.isVisible = false
                } else {
                    isVisible = false
                    repeatSavePost.isVisible = true
                }
            }
            postAttachment.apply {
                if (post.attachment != null && post.isOnServer) {
                    isVisible = true
                    contentDescription = post.attachment.description
                    load(
                        onInteractionListener.attachmentUrl(post.attachment.url),
                        post.attachment.type.name
                    )
                } else
                    isVisible = false
            }
            likes.isChecked = post.likedByMe
            likes.text = CountDisplay.show(post.likes)
            share.text = CountDisplay.show(post.shares)
            views.text = CountDisplay.show(post.views)
        }
    }

    private fun setupListeners(post: Post) {
        binding.apply {
            root.setOnClickListener {
                onInteractionListener.checkAuth()
                if (onInteractionListener.authorized)
                    onInteractionListener.toSinglePost(post)
            }
            likes.setOnClickListener {
                onInteractionListener.checkAuth()
                if (onInteractionListener.authorized)
                    onInteractionListener.onLike(post)
                else
                    likes.isChecked = false
            }
            // Click Share
            share.setOnClickListener {
                onInteractionListener.checkAuth()
                if (onInteractionListener.authorized)
                    onInteractionListener.onShare(post)
            }
            postAttachment.setOnClickListener {
                onInteractionListener.checkAuth()
                if (onInteractionListener.authorized)
                    onInteractionListener.onAttachments(post)
            }
            repeatSavePost.setOnClickListener {
                onInteractionListener.repeatSave(post)
            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            // Click Remove
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            // Click Edit
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
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