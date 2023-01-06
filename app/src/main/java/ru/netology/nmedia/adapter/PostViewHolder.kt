package ru.netology.nmedia.adapter

import android.view.View
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.util.CompanionNotMedia.actualTime

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
            avatar.setImageResource(
                if (author.text.contains("нетология", true))
                    R.drawable.netology
                else
                    if (author.text.contains("гитарин", true))
                        R.drawable.guitarin
                    else
                        R.drawable.ic_local_user_24
            )
            likes.isChecked = post.likedByMe
            likes.text = CountDisplay.show(post.likes)
            share.text = CountDisplay.show(post.shares)
            attachments.visibility =
                if (post.attachments != null)
                    View.VISIBLE
                else
                    View.INVISIBLE
            views.text = CountDisplay.show(post.views)
        }
    }

    private fun setupListeners(post: Post) {
        binding.apply {
            root.setOnClickListener {
                onInteractionListener.toSinglePost(post)
            }
            likes.setOnClickListener {
                onInteractionListener.onLike(post)
            }
            // Click Share
            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
            attachments.setOnClickListener {
                onInteractionListener.onAttachments(post)
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