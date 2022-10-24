package ru.netology.nmedia.viewmodel

import android.content.Context
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.R
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.AndroidUtils

private val empty = Post(
    id = 0,
    author = "",
    published = "",
    content = ""
)

class PostViewModel : ViewModel() {
    private val repository: PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.getAll()
    val edited = MutableLiveData(empty)

    private fun validation(view: EditText, context: Context): Boolean {
        return if (view.text.isNullOrBlank()) {
            Toast.makeText(
                context,
                R.string.empty_content,
                Toast.LENGTH_SHORT
            ).show()
            false
        } else
            true
    }

    private fun changeContent(content: String) {
        edited.value?.let {
            val text = content.trim()
            if (it.content != text)
                edited.value = it.copy(content = text)
        }
    }

    private fun save() {
        edited.value?.let { repository.save(it) }
        edited.value = empty
    }

    private fun removeFocus(view: EditText) {
        view.setText("")
        view.clearFocus()
        AndroidUtils.hideKeyboard(view)
    }

    fun savePost(view: EditText, context: Context) {
        view.apply {
            if (validation(this, context)) {
                changeContent(this.text.toString())
                save()
                removeFocus(this)
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun editPostContent(view: EditText, post: Post) {
        if (post.id != 0L)
            view.apply {
                requestFocus()
                setText(post.content)
            }
    }

    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
    fun viewById(id: Long) = repository.viewById(id)
    fun removeById(id: Long) = repository.removeById(id)
}