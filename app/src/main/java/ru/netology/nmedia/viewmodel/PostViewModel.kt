package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import ru.netology.nmedia.repository.*

class PostViewModel : ViewModel() {
    private val repository: PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.get()

    fun like() = repository.like()
    fun share() = repository.share()
    fun unshare() = repository.unshare()
    fun view() = repository.view()
    fun unview() = repository.unview()
}