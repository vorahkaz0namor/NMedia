package ru.netology.nmedia.repository

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.util.CompanionNotMedia.exceptionCheck
import ru.netology.nmedia.util.CompanionNotMedia.overview
import javax.inject.Inject

// В данном случае аннотация @Inject указывает на то, что
// реализация интерфейса PostRepository должна осуществляться
// на базе класса PostRepositoryImpl
class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val postApiService: PostApiService
): PostRepository {
    companion object {
        private const val AVATAR_PATH = "/avatars/"
        private const val IMAGE_PATH = "/media/"
    }

    override val data = dao.getAllReaded().map {
            it.map(PostEntity::toDto)
        }
    // Операции, которые требуют максимальных ресурсов, рекомендуется выполнять
    // на Dispatchers.Default, чтобы обеспечить максимальную производительность
        .flowOn(Dispatchers.Default)

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
                        val unread = dao.getUnread().size
                        emit(unread)
                    } else
                        throw HttpException(postsResponse)
                    // По правилам обработки исключений, возникающих в корутинах на уровнях,
                    // находящихся ниже ViewModel, крайне желательно CancellationException
                    // прокидывать на верхний уровень (уровень ViewModel)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    println("CAUGHT EXCEPTION WHEN GET NEWER => $e\n" +
                            "DESCRIPTION => ${overview(exceptionCheck(e))}\n")
                }
            }
        }
            .flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        // Асинхронно вызываем сетевой запрос с помощью функции getAll()
        val postsResponse = postApiService.getAll()
        if (postsResponse.isSuccessful) {
            val postsFromResponse = postsResponse.body().orEmpty().sortedBy { it.id }
            // { PostEntity.fromDto(it) } => Convert lambda to reference =>
            // => (PostEntity::fromDto)
            updatePostsByIdFromServer(postsFromResponse, false).map {
                dao.removeById(it.id)
            }
        } else
            throw HttpException(postsResponse)
    }

    override suspend fun showUnreadPosts() {
        dao.getUnread().map {
            dao.updateHiddenToFalse(it)
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
            val localSavedPostId = dao.save(PostEntity.fromDto(post))
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
                dao.save(PostEntity.fromDto(
                    savedPost.copy(id = localSavedPostId, idFromServer = savedPost.id)
                ))
            }
            else
                throw HttpException(postResponse)
        } catch (e: Exception) {
            Log.d("SAVING WITH ATTACHMENT", "CAUGHT EXCEPTION => $e\n" +
                    "DESCRIPTION => ${overview(exceptionCheck(e))}")
        }
    }

    override suspend fun save(post: Post) {
        val localSavedPostId = dao.save(PostEntity.fromDto(post))
        val postResponse = postApiService.savePost(post.copy(id = post.idFromServer))
        if (postResponse.isSuccessful) {
            val savedPost = postResponse.body() ?: throw HttpException(postResponse)
            dao.save(PostEntity.fromDto(
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
        val allExistingPosts = dao.getAll()
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
        dao.updatePostsByIdFromServer(loadedPosts)
        return postsToDelete
    }

    override suspend fun likeById(
        id: Long,
        idFromServer: Long,
        likedByMe: Boolean
    ) {
        dao.likeById(id)
        val postResponse = postApiService.let {
            if (likedByMe)
                it.unlikeById(idFromServer)
            else
                it.likeById(idFromServer)
        }
        if (postResponse.isSuccessful) {
            val loadedPost = postResponse.body() ?: throw HttpException(postResponse)
            dao.updatePostsByIdFromServer( listOf( PostEntity.fromDto(
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
        dao.removeById(id)
        if (idFromServer != 0L) {
            val response = postApiService.removeById(idFromServer)
            if (response.isSuccessful)
                showUnreadPosts()
            else
                throw HttpException(response)
        }
    }

    override suspend fun viewById(id: Long) = dao.viewById(id)

    override fun avatarUrl(authorAvatar: String) = "${BuildConfig.BASE_URL}$AVATAR_PATH$authorAvatar"

    override fun attachmentUrl(url: String) = "${BuildConfig.BASE_URL}$IMAGE_PATH$url"
}