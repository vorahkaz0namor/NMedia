package ru.netology.nmedia.repository

import retrofit2.HttpException
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.auth.AppAuth

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

    override suspend fun register(name: String, login: String, password: String) {
        val response = PostApi.service.register(login, password, name)
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