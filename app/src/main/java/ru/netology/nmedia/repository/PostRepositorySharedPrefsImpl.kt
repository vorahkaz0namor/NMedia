package ru.netology.nmedia.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post

class PostRepositorySharedPrefsImpl(
    context: Context
) : PostRepositoryInMemoryImpl() {
    private val prefs = context.getSharedPreferences("nmediarepo", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
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