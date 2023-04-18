package ru.netology.nmedia.adapter

import android.util.Log
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.ItemLoadingBinding

class PostLoadingViewHolder(
    private val binding: ItemLoadingBinding,
    private val retryListener: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(loadState: LoadState) {
        Log.d("INCOMING LOADSTATE", "$loadState")
        binding.apply {
            progressLoading.isVisible =
                loadState is LoadState.Loading
            retryLoading.isVisible =
                loadState is LoadState.Error
            retryLoading.setOnClickListener {
                retryListener()
            }
        }
    }
}