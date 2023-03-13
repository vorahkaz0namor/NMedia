package ru.netology.nmedia.repository

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.model.MediaModel

class AuthRepositoryImpl : AuthRepository {
    override suspend fun login(login: String, password: String) {
        val response = PostApi.service.login(login, password)
        if (response.isSuccessful) {
            val authModel = response.body() ?: throw HttpException(response)
            AppAuth.getInstance().setAuth(authModel)
        }
        else
            throw HttpException(response)
    }

    override suspend fun register(name: String, login: String, password: String, media: MediaModel?) {
        val response =
            PostApi.service.let {
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
            AppAuth.getInstance().setAuth(authModel)
        }
        else
            throw HttpException(response)
    }

    override suspend fun logout() {
        AppAuth.getInstance().removeAuth()
    }
}