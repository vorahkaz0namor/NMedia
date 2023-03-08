package ru.netology.nmedia.repository

interface AuthRepository {
    suspend fun login(login: String, password: String)
    suspend fun register(name: String, login: String, password: String)
    suspend fun logout()
}