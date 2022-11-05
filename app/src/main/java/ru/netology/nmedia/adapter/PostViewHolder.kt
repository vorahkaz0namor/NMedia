package ru.netology.nmedia.adapter

import android.view.View
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.*

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
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
            if (post.attachments.isNotEmpty()) {
                attachments.text = CountDisplay.show(post.attachments.size)
                attachments.visibility = View.VISIBLE
            }
            else
                attachments.visibility = View.INVISIBLE
            views.text = CountDisplay.show(post.views)
            // Click Like
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