package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post

class PostAdapter(
    private val likeClickListener: OnViewListener,
    private val shareClickListener: OnViewListener,
    private val viewClickListener: OnViewListener
) : ListAdapter<Post, PostViewHolder>(PostItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(
            binding,
            likeClickListener,
            shareClickListener,
            viewClickListener
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) =
        holder.bind(getItem(position))
}