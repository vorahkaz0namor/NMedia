package ru.netology.nmedia.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.*

private val empty = Post(
    id = 0,
    author = "",
    published = "",
    content = ""
)

class PostViewModel : ViewModel() {
    private val repository: PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.getAll()
    // Variable to hold editing post
    val edited = MutableLiveData(empty)

    private fun validation(text: CharSequence?) = (!text.isNullOrBlank())

    private fun changeContent(content: String) {
        edited.value?.let {
            val text = content.trim()
            if (it.content != text)
                edited.value = it.copy(content = text)
        }
    }

    private fun save() {
        edited.value?.let { repository.save(it) }
    }

    fun clearEditedValue() {
        edited.value = empty
    }

    fun savePost(text: CharSequence?): Boolean {
        return if (validation(text)) {
                   changeContent(text.toString())
                   save()
                   true
               }
               else
                   false
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
    fun viewById(id: Long) = repository.viewById(id)
    fun removeById(id: Long) = repository.removeById(id)
}