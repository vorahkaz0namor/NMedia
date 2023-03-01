package ru.netology.nmedia.repository

interface AuthRepository {
    suspend fun login(login: String, password: String)
    suspend fun logout()
}