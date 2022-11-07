package ru.netology.nmedia.repository

import android.content.Context

class PostRepositorySharedPrefsImpl(
    context: Context
) : PostRepositoryInMemoryImpl() {
    private val prefs = context.getSharedPreferences("nmediarepo", Context.MODE_PRIVATE)
    private val key = "posts"

    init {
        prefs.getString(key, null)?.let {
            posts = gson.fromJson(it, type)
            super.sync()
        }
    }

    override fun sync() {
        super.sync()
        prefs.edit().apply {
            putString(key, gson.toJson(posts))
            apply()
        }
    }
}