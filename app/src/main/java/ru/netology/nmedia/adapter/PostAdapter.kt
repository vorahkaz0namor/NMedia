package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post

class PostAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<Post, PostViewHolder>(PostItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(
            binding,
            onInteractionListener
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        // Поскольку PagingDataAdapter может возвращать пустые элементы в качестве
        // заглушек, то необходимо обработать такие ситуации.
        // В данном случае в классе PostRepositoryImpl явно указано, что заглушки
        // не используются (enablePlaceholders = false).
        // Поэтому можно игнорировать данный результат при возврате элемента по
        // позиции.
        getItem(position)?.let(holder::bind)
    }
}