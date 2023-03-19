package ru.netology.nmedia.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {
    companion object {
        private const val WORK_URL = "${BuildConfig.BASE_URL}/api/slow/"
    }
    @Singleton
    @Provides
    fun provideLogger(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    @Singleton
    @Provides
    fun provideClient(
        logger: HttpLoggingInterceptor,
        appAuth: AppAuth
    ): OkHttpClient = OkHttpClient.Builder()
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

    @Singleton
    @Provides
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(WORK_URL)
        // Функция addConverterFactory() указывает на то, что парсинг
        // будет происходить "под капотом"
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    @Singleton
    @Provides
    fun postApiService(
        retrofit: Retrofit
    ): PostApiService =
        // В Kotlin есть функция-расширение create(), которая по типу
        // создаваемой переменной service определяет и передает параметр
        // для функции create() библиотеки Retrofit
        retrofit.create()
}