package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post

class PostAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val pathPointer: PathPointer
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostItemCallback()) {
    // Тип элемента можно получить из данных, для чего нужно переопределить
    // метод getItemViewType()
    override fun getItemViewType(position: Int): Int =
        // Для типа элемента ловчее всего использовать ссылку на его макет
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            null -> error("Unknown item type")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding = CardPostBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }
            R.layout.card_ad -> {
                val binding = CardAdBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding, pathPointer)
            }
            else -> error("Unknown view type: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Поскольку PagingDataAdapter может возвращать пустые элементы в качестве
        // заглушек, то необходимо обработать такие ситуации.
        // В данном случае в классе PostRepositoryImpl явно указано, что заглушки
        // не используются (enablePlaceholders = false).
        // Поэтому можно игнорировать данный результат при возврате элемента по
        // позиции.
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> {
                val previousPost = getPreviousPost(position)
                (holder as? PostViewHolder)?.bind(
                    previousPost = previousPost,
                    currentPost = item)
            }
            null -> error("Unknown item type")
        }
    }

    private fun getPreviousPost(position: Int): Post? {
        val previousItemPosition = position - 1
        val previousItem =
            if (previousItemPosition >= 0) {
                getItem(previousItemPosition)
            }
            else {
                null
            }
        val result =
            if (previousItem != null) {
                if (previousItem is Ad) {
                    val previousPostPosition = position - 2
                    if (previousPostPosition >= 0) {
                        getItem(previousPostPosition) as? Post
                    } else {
                        null
                    }
                }
                else {
                    previousItem as? Post
                }
        } else {
            null
        }
        return result
    }
}