package ru.netology.nmedia.api

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.model.AuthModel
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
    .addInterceptor { chain ->
        // В случае, если осуществлена авторизация и в SharedPreferences
        // записан токен, тогда на основе существующего запроса создается
        // новый, к которому добавляется дополнительный заголовок (Header)
        val newRequest =
            AppAuth.getInstance().data.value?.token?.let {
                chain.request().newBuilder()
                    .addHeader("Authorization", it)
                    .build()
            // Если авторизация еще не выполнена, тогда...
            } ?: chain.request()
        // ...прокидываем исходный запрос.
        chain.proceed(newRequest)
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
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun login(
        @Field("login") login: String,
        @Field("pass") password: String
    ): Response<AuthModel>

    @Multipart
    @POST("users/registration")
    suspend fun registerWithAvatar(
        @Part("login") login: RequestBody,
        @Part("pass") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part?
    ): Response<AuthModel>

    @POST("users/push-tokens")
    suspend fun sendPushToken(@Body token: PushToken)

    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @Multipart
    @POST("media")
    suspend fun uploadMedia(@Part part: MultipartBody.Part): Response<Media>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @POST("posts")
    suspend fun savePost(@Body body: Post): Response<Post>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun unlikeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>
}