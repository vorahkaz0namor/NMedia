package ru.netology.nmedia.di

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.AuthRepository
import ru.netology.nmedia.repository.AuthRepositoryImpl
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl

class DependencyContainer(
    private val context: Context
) {
    companion object {
        private const val WORK_URL = "${BuildConfig.BASE_URL}/api/slow/"
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: DependencyContainer? = null

        fun initApp(context: Context) {
            instance = DependencyContainer(context)
        }

        fun getInstance() = instance!!
    }
    private val appDb = Room.databaseBuilder(
        context,
        AppDb::class.java,
        "nmedia.db"
    )
        .build()
    private val postDao = appDb.postDao()
    private val workManager = WorkManager.getInstance(context)
    val appAuth = AppAuth(context, workManager)
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
                appAuth.data.value?.token?.let {
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
    val postApiService: PostApiService by lazy {
            // В Kotlin есть функция-расширение create(), которая по типу
            // создаваемой переменной service определяет и передает параметр
            // для функции create() библиотеки Retrofit
            retrofit.create()
        }
    val postRepository: PostRepository =
        PostRepositoryImpl(postDao, postApiService)
    val authRepository: AuthRepository =
        AuthRepositoryImpl(postApiService, appAuth)
}