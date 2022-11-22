package ru.netology.nmedia.viewmodel

import android.app.Application
import android.icu.text.SimpleDateFormat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.*
import java.util.*

private val empty = Post(
    id = 0,
    author = "",
    published = "",
    content = ""
)
private val actualTime = { now: Long ->
    SimpleDateFormat("dd MMMM, H:mm", Locale.US).format(Date(now))
}

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryRoomDBImpl(AppDb.getInstance(application).postDao())
    val data = repository.getAll()
    // Variable to hold editing post
    val edited = MutableLiveData(empty)
    // Variable to hold sharing post
    val hasShared = MutableLiveData(empty)
    // Variable to hold viewing post attachments
    val viewingAttachments = MutableLiveData(empty)
    // Variable to hold single post to view
    val singlePostToView = MutableLiveData(empty)

    private fun validation(text: CharSequence?) =
        (!text.isNullOrBlank() && edited.value?.content != text.trim())

    private fun save(newContent: String) {
        edited.value?.let {
            repository.save(
                it.copy(
                    author = if (it.id == 0L)
                        "Zakharov Roman, AN-34"
                    else
                        it.author,
                    content = newContent,
                    published = actualTime(System.currentTimeMillis())
                )
            )
        }
    }

    fun clearEditedValue() {
        edited.value = empty
    }

    fun saveDraftCopy(content: String?) {
        if (edited.value?.id == 0L)
           repository.saveDraftCopy(content)
    }

    fun getDraftCopy() = repository.getDraftCopy()

    fun savePost(text: CharSequence?): Long? {
        if (validation(text)) {
            save(text.toString())
        }
        val result = edited.value?.id
        clearEditedValue()
        return result
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(post: Post) {
        hasShared.apply {
            value = post
            repository.shareById(post.id)
            value = empty
        }
    }
    fun showAttachments(post: Post) {
        viewingAttachments.apply {
            value = post
            value = empty
        }
    }
    fun viewById(id: Long) = repository.viewById(id)
    fun removeById(id: Long) = repository.removeById(id)
    fun singlePost(post: Post) {
        singlePostToView.apply {
            value = post
            value = empty
        }
    }
}