package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.SendingPost
import java.util.concurrent.TimeUnit

class PostRepositoryImpl: PostRepository {
    // Создаем клиента с использованием паттерна Builder()
    private val client = OkHttpClient.Builder()
        // Ставим таймаут на сетевое соединение
//        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}
    private val callRequest = { request: Request ->
        client.newCall(request).execute().close()
    }
    companion object {
        // Базовый URL, с которым будем работать
        private const val BASE_URL = "http://192.168.31.16:9999"
        // Тип данных, указываемый в заголовке
        private val jsonType = "application/json".toMediaType()
        private const val PATH = "/api/slow/posts"
    }

    override fun getAll(): List<Post> {
        // Создание запроса
        val request: Request = Request.Builder()
            .url("$BASE_URL$PATH")
            .build()
        // Синхронно вызываем сетевой запрос с помощью функции newCall()
        return client.newCall(request)
            .execute().let { response: Response? ->
                response?.body?.string()
                    ?: throw java.lang.RuntimeException("The body is null")
            }
            .let {
                gson.fromJson(it, typeToken.type)
            }
    }

    override fun save(post: Post) {
        val request: Request = Request.Builder()
            .post(gson.toJson(SendingPost.fromDto(post)).toRequestBody(jsonType))
            .url("$BASE_URL$PATH")
            .build()

        callRequest(request)
    }

    override fun likeById(id: Long, likedByMe: Boolean) {
        val request: Request = Request.Builder().let {
            if (likedByMe)
                it.delete()
            else
                it.post("".toRequestBody())
        }.url("$BASE_URL$PATH/$id/likes")
         .build()

        callRequest(request)
    }

    override fun viewById(id: Long) {
        val request: Request = Request.Builder()
            .post("".toRequestBody())
            .url("$BASE_URL$PATH/$id/views")
            .build()

        callRequest(request)
    }

    override fun removeById(id: Long) {
        val request: Request = Request.Builder()
            .delete()
            .url("$BASE_URL$PATH/$id")
            .build()

        callRequest(request)
    }
}