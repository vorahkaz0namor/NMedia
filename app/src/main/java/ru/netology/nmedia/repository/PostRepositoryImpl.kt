package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostRepositoryImpl: PostRepository {
    // Создаем клиента с использованием паттерна Builder()
    private val client = OkHttpClient.Builder()
        // Ставим таймаут на сетевое соединение
        .connectTimeout(30, TimeUnit.SECONDS)
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
    }

    override fun getAll(): List<Post> {
        // Создание запроса
        val request: Request = Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .build()
        // Синхронно вызываем сетевой запрос с помощью функции newCall()
        return client.newCall(request)
            .execute().let { response ->
                val body: String? = response.body?.string()
                body ?: throw java.lang.RuntimeException("The body is null")
            }
            .let {
                val posts: List<Post> =
                    try {
                        gson
                            .fromJson(it, typeToken.type)
                    } catch (e: JsonSyntaxException) {
                        println("$e: I'm getting lost here...")
                        emptyList()
                    }
                posts
            }
    }

    override fun save(post: Post) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("$BASE_URL/api/slow/posts")
            .build()

        callRequest(request)
    }

    override fun likeById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun shareById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun viewById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun removeById(id: Long) {
        val request: Request = Request.Builder()
            .delete()
            .url("$BASE_URL/api/slow/posts/$id")
            .build()

        callRequest(request)
    }
}