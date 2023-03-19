package ru.netology.nmedia.repository

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.model.MediaModel
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val postApiService: PostApiService,
    private val appAuth: AppAuth
) : AuthRepository {
    override suspend fun login(login: String, password: String) {
        val response = postApiService.login(login, password)
        if (response.isSuccessful) {
            val authModel = response.body() ?: throw HttpException(response)
            appAuth.setAuth(authModel)
        }
        else
            throw HttpException(response)
    }

    override suspend fun register(name: String, login: String, password: String, media: MediaModel?) {
        val response =
            postApiService.let {
                val mediaAsRequest =
                    if (media != null)
                        MultipartBody.Part.createFormData(
                            "file", media.file.name, media.file.asRequestBody()
                        )
                    else
                        null
                it.registerWithAvatar(
                    login.toRequestBody("text/plain".toMediaType()),
                    password.toRequestBody("text/plain".toMediaType()),
                    name.toRequestBody("text/plain".toMediaType()),
                    mediaAsRequest
                )
            }
        if (response.isSuccessful) {
            val authModel = response.body() ?: throw HttpException(response)
            appAuth.setAuth(authModel)
        }
        else
            throw HttpException(response)
    }

    override suspend fun logout() {
        appAuth.removeAuth()
    }
}