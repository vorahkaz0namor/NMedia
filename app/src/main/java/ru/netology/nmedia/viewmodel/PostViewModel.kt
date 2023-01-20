package ru.netology.nmedia.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.repository.PostRepository.PostCallback
import ru.netology.nmedia.util.SingleLiveEvent

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
        // Включение состояния "загрузка"
        _data.value = _data.value?.loading()
        repository.getAll(object : PostCallback<List<Post>> {
            // Если данные успешно получены, то отправляем их в data
            override fun onSuccess(result: List<Post>) {
                _data.postValue(_data.value?.showing(result))
            }
            // Если получена ошибка
            override fun onError(e: Exception) {
                Log.d("EXCEPTION WHEN LOAD POSTS:", "$e")
                _data.postValue(_data.value?.error())
            }
        })
    }

    private fun currentPostsList() =  _data.value?.posts.orEmpty()

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
                    authorAvatar = if (it.id == 0L)
                        "localuser.jpg"
                    else
                        it.authorAvatar,
                    content = newContent,
                    published = System.currentTimeMillis()
                ),
                object : PostCallback<Int> {
                    override fun onSuccess(result: Int) {}
                    override fun onError(e: Exception) {
                        Log.d("SAVING EXCEPTION. CODE:", "$e")
                    }
                }
            )
            _postEvent.postValue(Unit)
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
        repository.likeById(
            post.id,
            post.likedByMe,
            object : PostCallback<Post> {
                override fun onSuccess(result: Post) {
                    _data.postValue(
                        _data.value?.showing(
                            currentPostsList().map {
                                if (it.id == post.id)
                                    result
                                else it
                            }
                        )
                    )
                }
                override fun onError(e: Exception) {
                    _data.postValue(_data.value?.error())
                }
            }
        )
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
            _data.value?.showing(
                currentPostsList().map {
                    if (it.id == id)
                        it.copy(views = it.views + 1)
                    else
                        it
                }
            )
        // Используется, когда viewById() реализован внутри сервера тоже
//        thread {
//            try {
//                repository.viewById(id)
//            } catch (_: IOException) {}
//        }
    }

    fun removeById(id: Long) {
        _data.value = _data.value?.loading()
        repository.removeById(id, object : PostCallback<Int> {
            override fun onSuccess(result: Int) {
                _data.postValue(
                    _data.value?.showing(
                        currentPostsList().filter { it.id != id }
                    )
                )
            }
            override fun onError(e: Exception) {
                Log.d("REMOVING EXCEPTION. CODE:", "$e")
                _data.postValue(_data.value?.showing(currentPostsList()))
            }
        })
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