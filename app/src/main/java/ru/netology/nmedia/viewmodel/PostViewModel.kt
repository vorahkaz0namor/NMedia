package ru.netology.nmedia.viewmodel

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
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
    // Variable to hold editing post
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
    }

    fun removeFocus(binding: ActivityMainBinding) {
        edited.value = empty
        binding.apply {
            editContent.setText("")
            editContent.clearFocus()
            AndroidUtils.hideKeyboard(editContent)
            editGroup.visibility = View.INVISIBLE
        }
    }

    fun savePost(binding: ActivityMainBinding, context: Context) {
        binding.apply {
            if (validation(editContent, context)) {
                changeContent(editContent.text.toString())
                save()
                removeFocus(this)
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun editPostContent(binding: ActivityMainBinding, post: Post) {
        if (post.id != 0L)
            binding.apply {
                editContent.requestFocus()
                editContent.setText(post.content)
                cancelEdit.visibility = View.VISIBLE
            }
    }

    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
    fun viewById(id: Long) = repository.viewById(id)
    fun removeById(id: Long) = repository.removeById(id)
}