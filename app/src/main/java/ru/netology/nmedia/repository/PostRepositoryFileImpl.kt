package ru.netology.nmedia.repository

import android.content.Context

class PostRepositoryFileImpl(
    private val context: Context
) : PostRepositoryInMemoryImpl() {

    private val filename = "nmediaposts.json"

    init {
        val file = context.filesDir.resolve(filename)
        if (file.exists()) {
            context
                .openFileInput(filename)
                .bufferedReader()
                .use {
                    posts = gson.fromJson(it, type)
                    super.sync()
                }
        } else
            sync()
    }

    override fun sync() {
        super.sync()
        context
            .openFileOutput(filename, Context.MODE_PRIVATE)
            .bufferedWriter()
            .use {
                it.write(gson.toJson(posts))
            }
    }
}