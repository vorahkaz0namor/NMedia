package ru.netology.nmedia.repository

import android.util.Log
import androidx.paging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.entity.DraftCopyEntity
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.util.CompanionNotMedia.customLog
import ru.netology.nmedia.util.CompanionNotMedia.listToString
import javax.inject.Inject
import kotlin.random.Random

// В данном случае аннотация @Inject указывает на то, что
// реализация интерфейса PostRepository должна осуществляться
// на базе класса PostRepositoryImpl
@OptIn(ExperimentalPagingApi::class)
class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val postApiService: PostApiService,
    private val postRemoteMediator: PostRemoteMediator
): PostRepository {
    companion object {
        private const val AVATAR_PATH = "/avatars/"
        private const val IMAGE_PATH = "/media/"
    }

    // Для аргумента PagingConfig указываются следующие аргументы:
    // - количество постов на странице (pageSize);
    // - показ временного изображения, которое отображается,
    //   пока не загрузится содержимое страницы (enablePlaceHolders).
    // PagingSourceFactory - лямбда-выражение, которое возвращает
    // объект PagingSource.
    // Функция flow() вернет поток типа PagingData.
    // ===> МОЖНО ПОПРОБОВАТЬ ПЕРЕДАТЬ СЮДА Pager ЧЕРЕЗ DI <===
    // Пока что через DI передается RemoteMediator
    override val data: Flow<PagingData<FeedItem>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
                postDao.getAllRead(
                    lastId = postRemoteKeyDao.before() ?: 0,
                    firstId = postRemoteKeyDao.after() ?: 0
                )
        },
        remoteMediator = postRemoteMediator
    ).flow
        .map {
            it.map(PostEntity::toDto)
            // Реализация вставки элементов с рекламой
                .insertSeparators { prev, _ ->
                    // Если это начало списка, то prev = null, а если конец, то next = null
                    // Функция rem(other: Int) возвращает остаток от деления на other
                    if (prev?.id?.rem(4) == 0L)
                        Ad(
                            id = Random.nextLong(),
                            image = "figma.jpg"
                        )
                    else
                        null
                }
        }

    override suspend fun getLatest(count: Int) {
        val response = postApiService.getLatest(count)
        if (response.isSuccessful) {
            val postsFromResponse = response.body().orEmpty().sortedBy { it.id }
            updatePostsByIdFromServer(postsFromResponse, false)
        } else
            throw HttpException(response)
    }

    override fun getNewerCount(latestId: Long): Flow<Int> =
        flow {
            emit(0)
            while (true) {
                delay(25 * 60 * 1_000)
                try {
                    val postsResponse = postApiService.getNewer(latestId)
                    if (postsResponse.isSuccessful) {
                        val newPosts = postsResponse.body().orEmpty().sortedBy { it.id }
                        updatePostsByIdFromServer(newPosts, true)
                        val unread = postDao.getUnread().size
                        emit(unread)
                    } else
                        throw HttpException(postsResponse)
                    // По правилам обработки исключений, возникающих в корутинах на уровнях,
                    // находящихся ниже ViewModel, крайне желательно CancellationException
                    // прокидывать на верхний уровень (уровень ViewModel)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    customLog("GET NEWER", e)
                }
            }
        }
            .flowOn(Dispatchers.Default)

    override suspend fun getPostById(id: Long): Post = postDao.getPostById(id).toDto()

    override suspend fun getAll() {
        // Асинхронно вызываем сетевой запрос с помощью функции getAll()
        val postsResponse = postApiService.getAll()
        if (postsResponse.isSuccessful) {
            val postsFromResponse = postsResponse.body().orEmpty().sortedBy { it.id }
            // { PostEntity.fromDto(it) } => Convert lambda to reference =>
            // => (PostEntity::fromDto)
            updatePostsByIdFromServer(postsFromResponse, false)
                // Удаление из БД несуществующих на сервере постов в случае
                // перезапуска сервера "с нуля"
                .map { postDao.removeById(it.id) }
        } else
            throw HttpException(postsResponse)
    }

    override suspend fun showUnreadPosts() {
        postDao.getUnread().map {
            postDao.updateHiddenToFalse(it)
        }
    }

    private suspend fun upload(media: MediaModel): Media {
        val part = MultipartBody.Part.createFormData(
            "file", media.file.name, media.file.asRequestBody()
        )
        val response =  postApiService.uploadMedia(part)
        if (!response.isSuccessful)
            throw HttpException(response)
        else
            return requireNotNull(response.body())
    }

    override suspend fun saveWithAttachment(post: Post, media: MediaModel) {
        try {
            val localSavedPostId = postDao.save(PostEntity.fromDto(post))
            val uploaded = upload(media)
            val postResponse = postApiService.savePost(
                post.copy(
                    id = post.idFromServer,
                    attachment = Attachment(
                        url = uploaded.id,
                        type = AttachmentType.IMAGE
                    )
                )
            )
            if (postResponse.isSuccessful) {
                val savedPost = postResponse.body() ?: throw HttpException(postResponse)
                postDao.save(PostEntity.fromDto(
                    savedPost.copy(id = localSavedPostId, idFromServer = savedPost.id)
                ))
            }
            else
                throw HttpException(postResponse)
        } catch (e: Exception) {
            customLog("SAVING WITH ATTACHMENT", e)
        }
    }

    override suspend fun save(post: Post) {
        val localSavedPostId = postDao.save(PostEntity.fromDto(post))
        val postResponse = postApiService.savePost(post.copy(id = post.idFromServer))
        if (postResponse.isSuccessful) {
            val savedPost = postResponse.body() ?: throw HttpException(postResponse)
            postDao.save(PostEntity.fromDto(
                savedPost.copy(id = localSavedPostId, idFromServer = savedPost.id)
            ))
        }
        else
            throw HttpException(postResponse)
    }

    private suspend fun updatePostsByIdFromServer(
        posts: List<Post>,
        hidden: Boolean
    ): List<PostEntity> {
        var loadedPosts: List<PostEntity> = emptyList()
        val allExistingPosts = postDao.getAll()
        var postsToDelete = allExistingPosts
        posts.map { singlePost ->
            val findExistingPost =
                allExistingPosts.find { it.idFromServer == singlePost.id }
            loadedPosts = if (findExistingPost != null)
                loadedPosts.plus(PostEntity.fromDto(
                    singlePost.copy(
                        id = findExistingPost.id,
                        idFromServer = findExistingPost.idFromServer,
                    )
                ).copy(hidden = hidden))
            else
                loadedPosts.plus(PostEntity.fromDto(
                    singlePost.copy(id = 0L, idFromServer = singlePost.id)
                ).copy(hidden = hidden))
            postsToDelete = postsToDelete.filter {
                it.idFromServer != 0L && it.idFromServer != singlePost.id
            }
        }
        // Контроль синхронизации постов, пришедших с сервера и постов,
        // находящихся в базе
        Log.d(
            "CTRL SYNC / uREPO",
            "form server => ${listToString(posts)}\n" +
                    "from DB => ${listToString(allExistingPosts)}\n" +
                    "to update => ${listToString(loadedPosts)}"
        )
        postDao.updatePostsByIdFromServer(loadedPosts)
        return postsToDelete
    }

    override suspend fun likeById(
        id: Long,
        idFromServer: Long,
        likedByMe: Boolean
    ) {
        postDao.likeById(id)
        val postResponse = postApiService.let {
            if (likedByMe)
                it.unlikeById(idFromServer)
            else
                it.likeById(idFromServer)
        }
        if (postResponse.isSuccessful) {
            val loadedPost = postResponse.body() ?: throw HttpException(postResponse)
            postDao.updatePostsByIdFromServer( listOf( PostEntity.fromDto(
                loadedPost.copy(
                    id = id,
                    idFromServer = idFromServer
                )
            )))
            showUnreadPosts()
        }
        else
            throw HttpException(postResponse)
    }

    override suspend fun removeById(id: Long, idFromServer: Long) {
        postDao.removeById(id)
        if (idFromServer != 0L) {
            val response = postApiService.removeById(idFromServer)
            if (response.isSuccessful)
                showUnreadPosts()
            else
                throw HttpException(response)
        }
    }

    override suspend fun viewById(id: Long) = postDao.viewById(id)

    override suspend fun getDraftCopy(): String = postDao.getDraftCopy()

    override suspend fun saveDraftCopy(content: String?) {
        postDao.clearDraftCopy()
        postDao.saveDraftCopy(DraftCopyEntity.fromDto(content))
    }

    override fun avatarUrl(authorAvatar: String) = "${BuildConfig.BASE_URL}$AVATAR_PATH$authorAvatar"

    override fun attachmentUrl(url: String) = "${BuildConfig.BASE_URL}$IMAGE_PATH$url"
}