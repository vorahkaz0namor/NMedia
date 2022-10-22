package ru.netology.nmedia.adapter

import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.*

typealias OnViewListener = (Post) -> Boolean

class PostViewHolder(
    private val binding: CardPostBinding,
    private val likeClickListener: OnViewListener,
    private val shareClickListener: OnViewListener,
    private val viewClickListener: OnViewListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            avatar.setImageResource(R.drawable.netology)
            likes.setImageResource(
                if (post.likedByMe)
                    R.drawable.ic_liked_24
                else
                    R.drawable.ic_baseline_favorite_border_24
            )
            likesCount.text = CountDisplay.show(post.likes)
            sharesCount.text = CountDisplay.show(post.shares)
            viewsCount.text = CountDisplay.show(post.views)
            // Click Like
            likes.setOnClickListener {
                likeClickListener(post)
            }
            // Click Share
            share.setOnClickListener {
                shareClickListener(post)
            }
            // Click View
            views.setOnClickListener {
                viewClickListener(post)
            }
        }
    }
}