package ru.netology.nmedia.adapter

import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.util.CompanionNotMedia.load

class AdViewHolder(
    private val binding: CardAdBinding,
    private val pathPointer: PathPointer
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        binding.adImage.load(pathPointer.attachmentUrl(ad.image), ad.type)
    }
}