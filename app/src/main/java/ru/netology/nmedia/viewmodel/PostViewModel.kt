package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import okhttp3.internal.http.*
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.CompanionNotMedia.exceptionCheck
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    author = "",
    published = 0,
    content = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(application).postDao())
    val data: LiveData<FeedModel> = repository.data.map { FeedModel(posts = it) }
    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState
    // Ручная реализация паттерна "слушатель-издатель" (одиночное событие),
    // дополнительно - перехватывает HTTP-код ответа сервера
    private val _postEvent = SingleLiveEvent(HTTP_CONTINUE)
    val postEvent: LiveData<Int>
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

    fun loadPosts() =
        // Для запуска корутин внутри ViewModel существует специальная
        // функция-расширение viewModelScope
        viewModelScope.launch {
            try {
                // Включение состояния "загрузка"
                _dataState.value = _dataState.value?.loading()
                repository.getAll()
                _dataState.value = _dataState.value?.showing()
            } catch (e: Exception) {
                _dataState.value = _dataState.value?.error()
                _postEvent.value = exceptionCheck(e)
            }
        }

    fun refresh() {
        viewModelScope.launch {
            try {
                _dataState.value = _dataState.value?.refreshing()
                repository.getAll()
                _dataState.value = _dataState.value?.showing()
            } catch (e: Exception) {
                _dataState.value = _dataState.value?.error()
                _postEvent.value = exceptionCheck(e)
            }
        }
    }

    private fun currentPostsList() = data.value?.posts.orEmpty()

    private fun validation(text: CharSequence?) =
        (!text.isNullOrBlank() && edited.value?.content != text.trim())

    private fun save(newContent: String) {
        viewModelScope.launch {
            try {
                edited.value?.let {
                    repository.save(
                        it.copy(
                            author = if (it.id == 0L)
                                "Zakharov Roman, AN-34"
                            else
                                it.author,
                            authorAvatar = if (it.id == 0L)
                                "localuser.jpg"
                            else
                                it.authorAvatar,
                            content = newContent,
                            published = System.currentTimeMillis()
                        )
                    )
                }
                _dataState.value = _dataState.value?.showing()
                _postEvent.value = HTTP_OK
            } catch (e: Exception) {
                _postEvent.value = exceptionCheck(e)
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

    fun savePost(text: CharSequence?) {
        if (validation(text)) {
            save(text.toString())
        } else
            _postEvent.value = HTTP_OK
    }

    fun repeatSavePost(post: Post) {
        viewModelScope.launch {
            try {
                _dataState.value = _dataState.value?.loading()
                repository.save(post)
                _dataState.value = _dataState.value?.showing()
                _postEvent.value = HTTP_OK
            } catch (e: Exception) {
                _postEvent.value = exceptionCheck(e)
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun likeById(post: Post) {
        viewModelScope.launch {
            try {
                _dataState.value = _dataState.value?.loading()
                repository.likeById(
                    post.id,
                    post.idFromServer,
                    post.likedByMe
                )
                _dataState.value = _dataState.value?.showing()
            } catch (e: Exception) {
                _dataState.value = _dataState.value?.error()
                _postEvent.value = exceptionCheck(e)
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
        viewModelScope.launch {
            repository.viewById(id)
        }
    }

    fun removeById(id: Long, idFromServer: Long) {
        viewModelScope.launch {
            try {
                _dataState.value = _dataState.value?.loading()
                repository.removeById(id, idFromServer)
                _dataState.value = _dataState.value?.showing()
            } catch (e: Exception) {
                _dataState.value = _dataState.value?.error()
                _postEvent.value = exceptionCheck(e)
            }
        }
    }

    fun singlePost(post: Post) {
        singlePostToView.apply {
            value = post
            value = empty
        }
    }

    fun getAvatarUrl(authorAvatar: String) = repository.avatarUrl(authorAvatar)

    fun getAttachmentUrl(url: String) = repository.attachmentUrl(url)
}