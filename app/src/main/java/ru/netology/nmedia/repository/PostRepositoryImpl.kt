package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.SendingPost
import ru.netology.nmedia.repository.PostRepository.PostCallback
import java.lang.Exception

class PostRepositoryImpl: PostRepository {
    // Создаем клиента с использованием паттерна Builder()
    private val client = OkHttpClient.Builder()
        // Ставим таймаут на сетевое соединение
//        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}
    private val callRequest = { request: Request, callback: PostCallback<Int> ->
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful)
                    callback.onSuccess(response.code)
                else
                    callback.onError(Exception("${response.code}"))
                response.body.close()
            }
        })
    }
    companion object {
        // Базовый URL, с которым будем работать
        private const val BASE_URL = "http://192.168.31.16:9999"
        // Тип данных, указываемый в заголовке
        private val jsonType = "application/json".toMediaType()
        private const val PATH = "/api/slow/posts"
        private const val AVATAR_PATH = "/avatars/"
    }

    override fun getAll(callback: PostCallback<List<Post>>) {
        // Создание запроса
        val request: Request = Request.Builder()
            .url("$BASE_URL$PATH")
            .build()
        // Синхронно вызываем сетевой запрос с помощью функции newCall()
        return client.newCall(request)
            .enqueue(object : Callback {
                // Этот вариант срабатывает в таких случаях, как, например,
                // отсутствие соединения, недоступен сервер и т.д.
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
                // Если же сервер хоть что-то ответил (выдал какой-либо код ответа),
                // то будет выполняться эта функция.
                override fun onResponse(call: Call, response: Response) {
                    // При этом здесь необходимо обработать неожиданные варианты ответа
                    callback.apply {
                        if (response.isSuccessful) {
                            val data: List<Post>? = response.body.string().let {
                                gson.fromJson(it, typeToken.type)
                            }
                            if (data != null)
                                onSuccess(data)
                            else
                                onError(Exception("Body is null"))
                        } else
                            onError(Exception(response.message))
                    }
                }
            })
    }

    override fun save(post: Post, callback: PostCallback<Int>) {
        val request: Request = Request.Builder()
            .post(gson.toJson(SendingPost.fromDto(post)).toRequestBody(jsonType))
            .url("$BASE_URL$PATH")
            .build()

        callRequest(request, callback)
    }

    override fun likeById(
        id: Long,
        likedByMe: Boolean,
        callback: PostCallback<Post>
    ) {
        val request: Request = Request.Builder().let {
            if (likedByMe)
                it.delete()
            else
                it.post("".toRequestBody())
        }.url("$BASE_URL$PATH/$id/likes")
         .build()

        return client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
                override fun onResponse(call: Call, response: Response) {
                    callback.apply {
                        if (response.isSuccessful) {
                            val post: Post? = response.body.string().let {
                                gson.fromJson(it, Post::class.java)
                            }
                            if (post != null)
                                onSuccess(post)
                            else
                                onError(Exception("Body is null"))
                        } else
                            onError(Exception(response.message))
                    }
                }
            })
    }

    override fun removeById(id: Long, callback: PostCallback<Int>) {
        val request: Request = Request.Builder()
            .delete()
            .url("$BASE_URL$PATH/$id")
            .build()

        callRequest(request, callback)
    }

    override fun avatarUrl(authorAvatar: String) = "$BASE_URL$AVATAR_PATH$authorAvatar"
}