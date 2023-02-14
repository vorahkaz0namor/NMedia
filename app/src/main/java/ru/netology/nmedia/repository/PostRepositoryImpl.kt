package ru.netology.nmedia.repository

import androidx.lifecycle.map
import retrofit2.HttpException
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

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

    override suspend fun getAll() {
        // Асинхронно вызываем сетевой запрос с помощью функции getAll()
        val postsResponse = PostApi.service.getAll()
        if (postsResponse.isSuccessful) {
            val posts = postsResponse.body().orEmpty().sortedBy { it.id }
            // { PostEntity.fromDto(it) } => Convert lambda to reference =>
            // => (PostEntity::fromDto)
            posts.map { loadedPost ->
                val existingPost = data.value?.find { it.idFromServer == loadedPost.id }
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
    ): Post {
        dao.likeById(id)
        val postResponse = PostApi.service.let {
            if (likedByMe)
                it.unlikeById(idFromServer)
            else
                it.likeById(idFromServer)
        }
        if (postResponse.isSuccessful)
            return postResponse.body() ?: throw HttpException(postResponse)
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