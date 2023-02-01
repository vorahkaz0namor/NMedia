package ru.netology.nmedia.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

private const val WORK_URL = "${BuildConfig.BASE_URL}/api/slow/"
private val logger = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BASIC
}
private val client = OkHttpClient.Builder()
//    .connectTimeout(30, TimeUnit.SECONDS)
    .let {
        if (BuildConfig.DEBUG)
            it.addInterceptor(logger)
        else
            it
    }
    .build()
private val retrofit = Retrofit.Builder()
    .baseUrl(WORK_URL)
    // Функция addConverterFactory() указывает на то, что парсинг
    // будет происходить "под капотом"
    .addConverterFactory(GsonConverterFactory.create())
    .client(client)
    .build()
object PostApi {
    val service: PostApiService by lazy {
        // В Kotlin есть функция-расширение create(), которая по типу
        // создаваемой переменной service определяет и передает параметр
        // для функции create() библиотеки Retrofit
        retrofit.create()
    }
}

interface PostApiService {
    @GET("posts")
    fun getAll(): Call<List<Post>>

    @POST("posts")
    fun savePost(@Body body: Post): Call<Post>

    @POST("posts/{id}/likes")
    fun likeById(@Path("id") id: Long): Call<Post>

    @DELETE("posts/{id}/likes")
    fun unlikeById(@Path("id") id: Long): Call<Post>

    @DELETE("posts/{id}")
    fun removeById(@Path("id") id: Long): Call<Unit>
}