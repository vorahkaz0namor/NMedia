package ru.netology.nmedia.repository

import androidx.lifecycle.asLiveData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.util.CompanionNotMedia.exceptionCheck
import ru.netology.nmedia.util.CompanionNotMedia.overview

class PostRepositoryImpl(
    private val dao: PostDao
): PostRepository {
    companion object {
        private const val AVATAR_PATH = "/avatars/"
        private const val IMAGE_PATH = "/images/"
    }

    override val data = dao.getAll().map {
            it.map(PostEntity::toDto)
        }
    // Операции, которые требуют максимальных ресурсов, рекомендуется выполнять
    // на Dispatchers.Default, чтобы обеспечить максимальную производительность
        .flowOn(Dispatchers.Default)

    override fun getNewerCount(latestId: Long): Flow<Int> =
        flow {
            while (true) {
                delay(30_000)
                try {
                    val postsResponse = PostApi.service.getNewer(latestId)
                    println("\nSIZE OF UNREAD POSTS LIST => ${postsResponse.body()?.size}\n\n")
                    if (postsResponse.isSuccessful) {
                        val newPosts = postsResponse.body().orEmpty().sortedBy { it.id }
                        newPosts.map {
                            dao.insert(
                                PostEntity.fromDto(
                                    it.copy(id = 0L, idFromServer = it.id)
                                ).copy(hidden = true)
                            )
                        }
                        val unread = dao.getUnread().size
                        println("\nFRESH UNREAD SIZE FROM DB => $unread")
                        emit(unread)
                    } else
                        throw HttpException(postsResponse)
                    // По правилам обработки исключений, возникающих в корутинах на уровнях,
                    // находящихся ниже ViewModel, крайне желательно CancellationException
                    // прокидывать на верхний уровень (уровень ViewModel)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    println("\nCAUGHT EXCEPTION => $e\n" +
                            "DESCRIPTION => ${overview(exceptionCheck(e))}\n")
                }
            }
        }
            .flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        // Асинхронно вызываем сетевой запрос с помощью функции getAll()
        val postsResponse = PostApi.service.getAll()
        if (postsResponse.isSuccessful) {
            val postsFromResponse = postsResponse.body().orEmpty().sortedBy { it.id }
            // { PostEntity.fromDto(it) } => Convert lambda to reference =>
            // => (PostEntity::fromDto)
//            data.collect {
//                it.map {
//                    println("\nID FROM SERVER, IN DB => ${it.idFromServer}\n\n")
//                }
//            }
//            val postsInDb: List<Post> = emptyList()
//            data.collect {
//                it.map { post ->
//                    postsInDb.plus(post)
//                }
//            }
            postsFromResponse.map { loadedPost ->
                val existingPost =
                    data.asLiveData().value?.let {
                        it.find { it.idFromServer == loadedPost.id }
                    }
//                    postsInDb.find {
//                        it.idFromServer == loadedPost.id
//                    }
                println("\nDETECTING EXISTING POST WHEN LOAD FROM SERVER, ID => " +
                        "${existingPost?.idFromServer}\n\n")
                if (existingPost != null)
                    dao.updatePostByIdFromServer(PostEntity.fromDto(
                            loadedPost.copy(
                                id = existingPost.id,
                                idFromServer = existingPost.idFromServer
                            )
                    ))
                else
                    dao.insert(PostEntity.fromDto(
                            loadedPost.copy(id = 0L, idFromServer = loadedPost.id)
                    ))
            }
        } else
            throw HttpException(postsResponse)
    }

    override suspend fun showUnreadPosts() {
        val unread = dao.getUnread()
        unread.map {
            dao.updateHiddenToFalse(it)
        }
    }

    override suspend fun save(post: Post) {
        val localSavedPostId = dao.save(PostEntity.fromDto(post))
        val postResponse = PostApi.service.savePost(post.copy(id = post.idFromServer))
        if (postResponse.isSuccessful) {
            val savedPost = postResponse.body() ?: throw HttpException(postResponse)
            dao.save(PostEntity.fromDto(
                savedPost.copy(id = localSavedPostId, idFromServer = savedPost.id)
            ))
        }
        else
            throw HttpException(postResponse)
    }

    override suspend fun likeById(
        id: Long,
        idFromServer: Long,
        likedByMe: Boolean
    ) {
        dao.likeById(id)
        val postResponse = PostApi.service.let {
            if (likedByMe)
                it.unlikeById(idFromServer)
            else
                it.likeById(idFromServer)
        }
        if (postResponse.isSuccessful) {
            val loadedPost = postResponse.body() ?: throw HttpException(postResponse)
            dao.updatePostByIdFromServer(PostEntity.fromDto(
                loadedPost.copy(
                    id = id,
                    idFromServer = idFromServer
                )
            ))
        }
        else
            throw HttpException(postResponse)
    }

    override suspend fun removeById(id: Long, idFromServer: Long) {
        dao.removeById(id)
        if (idFromServer != 0L) {
            val response = PostApi.service.removeById(idFromServer)
            if (!response.isSuccessful)
                throw HttpException(response)
        }
    }

    override suspend fun viewById(id: Long) = dao.viewById(id)

    override fun avatarUrl(authorAvatar: String) = "${BuildConfig.BASE_URL}$AVATAR_PATH$authorAvatar"

    override fun attachmentUrl(url: String) = "${BuildConfig.BASE_URL}$IMAGE_PATH$url"
}