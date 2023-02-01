package ru.netology.nmedia.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.internal.http.HTTP_CONTINUE
import okhttp3.internal.http.HTTP_OK
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.repository.PostRepository.PostCallback
import ru.netology.nmedia.util.CompanionNotMedia.overview
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

    fun loadPosts() {
        // Включение состояния "загрузка"
        _data.value = _data.value?.loading()
        repository.getAll(object : PostCallback<List<Post>> {
            // Если данные успешно получены, то отправляем их в data
            override fun onSuccess(result: List<Post>, code: Int) {
                // Поскольку Retrofit возвращает value в MainThread,
                // то вместо .postValue() можно смело использовать .value =
                _data.value = _data.value?.showing(posts = result, code = code)
            }
            // Если получена ошибка
            override fun onError(code: Int) {
                _data.value = _data.value?.error(code)
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
                object : PostCallback<Post> {
                    override fun onSuccess(result: Post, code: Int) {
                        _data.value =
                            _data.value?.showing(
                                posts = currentPostsList().let { postList ->
                                    if (postList.none { it.id == result.id })
                                        postList.plus(result).sortedByDescending { it.id }
                                    else
                                        postList.map { post ->
                                            if (post.id == result.id)
                                                post.copy(
                                                    content = result.content,
                                                    published = result.published
                                                )
                                            else
                                                post
                                        }
                                },
                                code = code
                            )
                        _postEvent.value = code
                    }
                    override fun onError(code: Int) {
                        _postEvent.value = code
                    }
                }
            )
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
        } else
            _postEvent.value = HTTP_CONTINUE
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
                override fun onSuccess(result: Post, code: Int) {
                    _data.value =
                        _data.value?.showing(
                            posts = currentPostsList().map {
                                if (it.id == post.id)
                                    result
                                else it
                            },
                            code = code
                        )
                }
                override fun onError(code: Int) {
                    _data.value = _data.value?.error(code)
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
                posts = currentPostsList().map {
                    if (it.id == id)
                        it.copy(views = it.views + 1)
                    else
                        it
                },
                code = HTTP_OK
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
        repository.removeById(id, object : PostCallback<Unit> {
            override fun onSuccess(result: Unit, code: Int) {
                _data.value =
                    _data.value?.showing(
                        posts = currentPostsList().filter { it.id != id },
                        code = code
                    )
            }
            override fun onError(code: Int) {
                Log.d("REMOVING EXCEPTION. CODE OVERVIEW:", overview(code))
                _data.value = _data.value?.showing(
                    posts = currentPostsList(),
                    code = code
                )
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