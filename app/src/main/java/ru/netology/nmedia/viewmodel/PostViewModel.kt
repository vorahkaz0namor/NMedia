package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.internal.http.*
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.CompanionNotMedia.customLog
import ru.netology.nmedia.util.CompanionNotMedia.exceptionCheck
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val empty = Post(
    id = 0,
    author = "",
    published = 0,
    content = ""
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
) : ViewModel() {
    private val defaultDispatcher = Dispatchers.Default
    private var _dataFlow: Flow<PagingData<FeedItem>>? = null
    val dataFlow: Flow<PagingData<FeedItem>>
        get() = cachedPagingDataFromRepo
    private val cachedPagingDataFromRepo =
        postRepository.data // Flow<PagingData<Post>>
            .flowOn(defaultDispatcher)
            .distinctUntilChanged()
            .cachedIn(viewModelScope)
    // Try to get newer posts, but this way don't work,
    // even don't compile
//    val newerCount: Flow<Int> =
//        data.switchMap { posts ->
//            postRepository.getNewerCount(
//                posts.map {
//                    it.idFromServer
//                }.filter {
//                    maxOf(it)
//                } ?: 0L
//            )
//    }
    private val _dataState = MutableLiveData(FeedModelState())
    val dataState: LiveData<FeedModelState>
        get() = _dataState
    private val _media = MutableLiveData<MediaModel?>(null)
    val media: LiveData<MediaModel?>
        get() = _media
    // Ручная реализация паттерна "слушатель-издатель" (одиночное событие),
    // дополнительно - перехватывает HTTP-код ответа сервера
    private val _postEvent = SingleLiveEvent(HTTP_CONTINUE)
    val postEvent: LiveData<Int>
        get() = _postEvent
//    @OptIn(ExperimentalCoroutinesApi::class)
//    val data: Flow<PagingData<Post>> =
//        appAuth.data // StateFlow<AuthModel?>
//            .flatMapLatest { authModel -> // it: AuthModel?
//                postRepository.data // Flow<PagingData<Post>>
//                    .map { posts -> // it: PagingData<Post>
//                        posts.map { post -> // it: Post
//                            post.copy(ownedByMe = authModel?.id == post.authorId)
//                        }
//                    }
//            }
//                .flowOn(defaultDispatcher)
//                .distinctUntilChanged()
    private val _draftCopy = MutableLiveData<String?>(null)
    val draftCopy: LiveData<String?>
        get() = _draftCopy
    // Variable to hold editing post
    val edited = MutableLiveData(empty)
    // Variable to hold sharing post
    val hasShared = MutableLiveData(empty)
    // Variable to hold viewing post attachments
    val viewingAttachments = MutableLiveData(empty)
    // Variable to hold single post to view
    val singlePostToView = MutableLiveData(empty)

//    init {
////        loadPosts()
//        flowPosts(true)
//    }

    fun loadPosts() =
        // Для запуска корутин внутри ViewModel существует специальная
        // функция-расширение viewModelScope
        viewModelScope.launch {
            try {
                // Включение состояния "загрузка"
                _dataState.value = _dataState.value?.loading()
                postRepository.getAll()
                _dataState.value = _dataState.value?.showing()
            } catch (e: Exception) {
                _dataState.value = _dataState.value?.error()
                _postEvent.value = exceptionCheck(e)
            }
        }

    fun flowPosts(initialState: Boolean = false) {
        viewModelScope.launch {
            try {
                _dataState.value =
                    if (initialState)
                        _dataState.value?.loading()
                    else
                        _dataState.value?.refreshing()
                _dataFlow = cachedPagingDataFromRepo.map { it }
                _dataState.value = _dataState.value?.showing()
            } catch (e: Exception) {
                _dataState.value = _dataState.value?.error()
                _postEvent.value = exceptionCheck(e)
                _dataFlow = null
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                _dataState.value = _dataState.value?.refreshing()
                postRepository.getAll()
                _dataState.value = _dataState.value?.showing()
            } catch (e: Exception) {
                _dataState.value = _dataState.value?.error()
                _postEvent.value = exceptionCheck(e)
            }
        }
    }

    fun refreshPagingData(initialState: Boolean = false) {
        viewModelScope.launch {
                try {
                    _dataState.value =
                        if (initialState)
                            _dataState.value?.loading()
                        else
                            _dataState.value?.refreshing()
                    dataFlow.collect()
                    _dataState.value = _dataState.value?.showing()
                } catch (e: Exception) {
                    _dataState.value = _dataState.value?.error()
                    _postEvent.value = exceptionCheck(e)
                }
        }
    }

    fun getPostById(id: Long): Post? {
        var result: Post? = null
        viewModelScope.launch {
            _dataState.value = _dataState.value?.loading()
            result = postRepository.getPostById(id)
            _dataState.value = _dataState.value?.showing()
        }
        return result
    }

    fun showUnreadPosts() {
        viewModelScope.launch {
            try {
                _dataState.value = _dataState.value?.loading()
                postRepository.showUnreadPosts()
                _dataState.value = _dataState.value?.showing()
            } catch (e: Exception) {
                _dataState.value = _dataState.value?.error()
                _postEvent.value = exceptionCheck(e)
            }
        }
    }

    private fun validation(text: CharSequence?) = (
            (!text.isNullOrBlank() &&
            edited.value?.content != text.trim()) ||
            media.value != null
    )

    private fun save(newContent: String) {
        viewModelScope.launch {
            try {
                edited.value?.let {
                    val post = it.copy(
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
                    when (val media = media.value) {
                        null -> postRepository.save(post)
                        else -> postRepository.saveWithAttachment(post, media)
                    }
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

    fun saveDraftCopy(content: String?) {
            viewModelScope.launch {
                if (edited.value?.id == 0L)
                    try {
                        postRepository.saveDraftCopy(content)
                    } catch (e: Exception) {
                        customLog("SAVING DRAFT COPY", e)
                    }
            }
    }

    fun getDraftCopy() {
        viewModelScope.launch {
            try {
                _draftCopy.value = postRepository.getDraftCopy()
            } catch (e: Exception) {
                customLog("GET DRAFT COPY", e)
            }
        }
    }

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
                postRepository.save(post)
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

    fun changePhoto(file: File, uri: Uri) {
        _media.value = MediaModel(uri, file)
    }

    fun clearPhoto() {
        _media.value = null
    }

    fun likeById(post: Post) {
        viewModelScope.launch {
            try {
                _dataState.value = _dataState.value?.loading()
                postRepository.likeById(
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
        viewingAttachments.value = post
    }

    fun clearAttachments() {
        viewingAttachments.value = empty
    }

    fun viewById(id: Long) {
        viewModelScope.launch {
            postRepository.viewById(id)
        }
    }

    fun removeById(id: Long, idFromServer: Long) {
        viewModelScope.launch {
            try {
                _dataState.value = _dataState.value?.loading()
                postRepository.removeById(id, idFromServer)
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
        }
    }

    fun clearSinglePostToView() {
        singlePostToView.value = empty
    }

    fun getAvatarUrl(authorAvatar: String) = postRepository.avatarUrl(authorAvatar)

    fun getAttachmentUrl(url: String) = postRepository.attachmentUrl(url)
}