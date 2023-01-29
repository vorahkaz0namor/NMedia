package ru.netology.nmedia.util

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import okhttp3.internal.http.HTTP_OK
import ru.netology.nmedia.R
import java.util.*

object CompanionNotMedia {
    var Bundle.POST_ID by LongArg
    var Bundle.POST_CONTENT by StringArg
    var Bundle.ATTACHMENT_PREVIEW by StringArg
    var Bundle.ATTACHMENT_URI by StringArg
    val actualTime = { now: Long ->
        SimpleDateFormat("dd MMMM, H:mm", Locale.US).format(Date(now))
    }
    val overview = { code: Int ->
        when (code) {
            in 200..299 -> if (code == 204) "Body is null" else "Successful"
            in 400..499 -> "Bad request"
            in 500..599 -> "Internal server error"
            else -> "Redirection"
        }
    }
    enum class Type {
        AVATAR,
        IMAGE
    }

    fun showToastAfterSave(
        fragmentContext: Context?,
        viewContext: Context,
        postId: Long?,
        initialContent: String?,
        newContent: String?,
        code: Int
    ) {
        if (newContent != initialContent)
            if (!newContent.isNullOrBlank())
                Toast.makeText(
                    fragmentContext,
                    viewContext.getString(
                        if (code == HTTP_OK) {
                            if (postId == 0L)
                                R.string.new_post_has_created
                            else
                                R.string.post_has_edited
                        } else
                            R.string.error_saving, overview(code)
                    ),
                    Toast.LENGTH_LONG
                ).show()
    }

    fun ImageView.load(
        url: String,
        type: String = Type.AVATAR.name,
        @DrawableRes placeholder: Int = R.drawable.ic_loading,
        @DrawableRes fallback: Int = R.drawable.ic_error_loading,
        timeOutMs: Int = 10_000
    ) {
        Glide.with(this)
            .load(url)
            .timeout(timeOutMs)
            .placeholder(placeholder)
            .error(fallback).apply {
                (if (type == Type.IMAGE.name)
                    dontTransform()
                else
                    circleCrop()
                ).into(this@load)
            }
    }
}