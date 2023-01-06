package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    author = "",
    published = 0,
    content = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    // Ручная реализация паттерна "слушатель-издатель" (одиночное событие)
    private val _postEvent = SingleLiveEvent<Unit>()
    val postEvent: LiveData<Unit>
        get() = _postEvent
    // Variable to hold editing post
    val edited = MutableLiveData(empty)
    // Variable to hold sharing post
    val hasShared = MutableLiveData(empty)
    // Variable to hold viewing post attachments
    val viewingAttachments = MutableLiveData(empty)
    // Variable to hold single post to view
    val singlePostToView = MutableLiveData(empty)

    init {
        loadPosts()
    }

    fun loadPosts() {
        thread {
            // Включение состояния "загрузка"
            _data.postValue(_data.value?.loading())
            try {
                val posts = repository.getAll()
                // Если данные успешно получены, то отправляем их в data
                _data.value?.showing(posts)
            } catch (e: IOException) {
                // Если получена ошибка
                _data.value?.error()
            }
                .also {
                    _data.postValue(it)
                }
            // Альтернативный вариант записи:
            /*.also(_data::postValue)*/
        }
    }

    private fun currentPostsList() =  _data.value?.posts.orEmpty()

    private fun validation(text: CharSequence?) =
        (!text.isNullOrBlank() && edited.value?.content != text.trim())

    private fun save(newContent: String) {
        edited.value?.let {
            thread {
                repository.save(
                    it.copy(
                        author = if (it.id == 0L)
                            "Zakharov Roman, AN-34"
                        else
                            it.author,
                        content = newContent,
                        published = System.currentTimeMillis()
                    )
                )
                _postEvent.postValue(Unit)
            }
        }
    }

    fun clearEditedValue() {
        edited.value = empty
    }

//    fun saveDraftCopy(content: String?) {
//        if (edited.value?.id == 0L)
//           repository.saveDraftCopy(content)
//    }
//
//    fun getDraftCopy() = repository.getDraftCopy()

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

    fun likeById(post: Post) {
        _data.value = _data.value?.loading()
        thread {
            try {
                val likedPost = repository.likeById(post.id, post.likedByMe)
                _data.postValue(
                    _data.value?.showing(
                        currentPostsList().map {
                            if (it.id == post.id)
                                likedPost
                            else it
                        }
                    )
                )
            } catch (_: IOException) {
                _data.postValue(_data.value?.error())
            }
        }
    }

    fun shareById(post: Post) {
        hasShared.apply {
            value = post
//            repository.shareById(post.id)
            value = empty
        }
    }

    fun showAttachments(post: Post) {
        viewingAttachments.apply {
            value = post
            value = empty
        }
    }

    fun viewById(id: Long) {
        _data.value =
            _data.value?.copy(
                posts = currentPostsList().map {
                    if (it.id == id)
                        it.copy(views = it.views + 1)
                    else
                        it
                }
            )
        thread {
            try {
                repository.viewById(id)
            } catch (_: IOException) {}
        }
    }

    fun removeById(id: Long) {
        _data.value = _data.value?.loading()
        thread {
            _data.postValue(
                _data.value?.showing(
                    currentPostsList().filter { it.id != id }
                )
            )
            try {
                repository.removeById(id)
            } catch (e: IOException) {
                _data.postValue(_data.value?.showing(currentPostsList()))
            }
        }
    }

    fun singlePost(post: Post) {
        singlePostToView.apply {
            value = post
            value = empty
        }
    }
}