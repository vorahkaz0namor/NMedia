package ru.netology.nmedia.repository

import ru.netology.nmedia.model.MediaModel

interface AuthRepository {
    suspend fun login(login: String, password: String)
    suspend fun register(name: String, login: String, password: String, media: MediaModel?)
    suspend fun logout()
}