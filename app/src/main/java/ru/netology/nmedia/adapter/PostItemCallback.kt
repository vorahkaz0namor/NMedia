package ru.netology.nmedia.adapter

import androidx.recyclerview.widget.DiffUtil
import ru.netology.nmedia.dto.FeedItem

class PostItemCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        // Чтобы не возникла ситуация, когда произойдет сравнение рекламы и поста,
        // у которых одинаковые id, и они засчитались как один и тот же объект,
        // надо выполнить проверку на соответствие классов
        if (oldItem::class == newItem::class)
            oldItem.id == newItem.id
        // Если классы объектов не совпадают, то элементы не равны между собой
        else
            false

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        oldItem == newItem
}