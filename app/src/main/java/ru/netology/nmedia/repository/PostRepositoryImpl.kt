package ru.netology.nmedia.repository

import okhttp3.internal.http.HTTP_INTERNAL_SERVER_ERROR
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository.PostCallback

class PostRepositoryImpl: PostRepository {
    companion object {
        private const val AVATAR_PATH = "/avatars/"
        private const val IMAGE_PATH = "/images/"
    }

    override fun getAll(callback: PostCallback<List<Post>>) {
        // Асинхронно вызываем сетевой запрос с помощью функции getAll()
        PostApi.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                // Если сервер хоть что-то ответил (выдал какой-либо код ответа),
                // то будет выполняться эта функция.
                override fun onResponse(
                    call: Call<List<Post>>,
                    response: Response<List<Post>>
                ) {
                    // Но при этом здесь необходимо обработать неожиданные варианты ответа
                    callback.apply {
                        if (response.isSuccessful) {
                            val posts = response.body()
                            if (posts != null)
                                onSuccess(posts, response.code())
                            else
                                onError(response.code())
                        } else
                            onError(response.code())
                    }
                }
                // Этот вариант срабатывает в таких случаях, как, например,
                // отсутствие соединения, недоступен сервер и т.д.
                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    callback.onError(HTTP_INTERNAL_SERVER_ERROR)
                }
            })
    }

    override fun save(post: Post, callback: PostCallback<Post>) {
        PostApi.service.savePost(post)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (response.isSuccessful) {
                        val savedPost = response.body()
                        if (savedPost != null)
                            callback.onSuccess(savedPost, response.code())
                        else
                            callback.onError(response.code())
                    }
                    else
                        callback.onError(response.code())
                }
                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(HTTP_INTERNAL_SERVER_ERROR)
                }
            })
    }

    override fun likeById(
        id: Long,
        likedByMe: Boolean,
        callback: PostCallback<Post>
    ) {
        PostApi.service.let {
            if (likedByMe)
                it.unlikeById(id)
            else
                it.likeById(id)
        }.enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                callback.apply {
                    if (response.isSuccessful) {
                        val post = response.body()
                        if (post != null)
                            onSuccess(post, response.code())
                        else
                            onError(response.code())
                    } else
                        onError(response.code())
                }
            }
            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(HTTP_INTERNAL_SERVER_ERROR)
            }
        })
    }

    override fun removeById(id: Long, callback: PostCallback<Unit>) {
        PostApi.service.removeById(id)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful)
                        callback.onSuccess(Unit, response.code())
                    else
                        callback.onError(response.code())
                }
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback.onError(HTTP_INTERNAL_SERVER_ERROR)
                }
            })
    }

    override fun avatarUrl(authorAvatar: String) = "${BuildConfig.BASE_URL}$AVATAR_PATH$authorAvatar"

    override fun attachmentUrl(url: String) = "${BuildConfig.BASE_URL}$IMAGE_PATH$url"
}