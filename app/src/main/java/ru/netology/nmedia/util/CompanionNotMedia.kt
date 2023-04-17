package ru.netology.nmedia.util

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.paging.CombinedLoadStates
import com.bumptech.glide.Glide
import okhttp3.internal.http.HTTP_FORBIDDEN
import okhttp3.internal.http.HTTP_NOT_FOUND
import okhttp3.internal.http.HTTP_NO_CONTENT
import retrofit2.HttpException
import ru.netology.nmedia.R
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import java.net.ConnectException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CompanionNotMedia {
    /** `520 Unknown Error` (non-standard HTTP code CloudFlare)  */
    private const val HTTP_UNKNOWN_ERROR = 520
    /** `444 Connection Failed` (thought up code)  */
    private const val HTTP_CONNECTION_FAILED = 444
    var Bundle.POST_ID by LongArg
    var Bundle.POST_CONTENT by StringArg
    var Bundle.ATTACHMENT_PREVIEW by StringArg
    var Bundle.ATTACHMENT_URI by StringArg
    // Коэффициент перевода значения вермени в зависимости от того,
    // в каком виде оно представлено - в секундах или в милисекундах
    val timeInHumanRepresentation = { now: Long ->
        OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(now),
            ZoneId.systemDefault()
        ).format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss"))
    }
    val overview = { code: Int ->
        when (code) {
            in 200..299 -> if (code == HTTP_NO_CONTENT)
                                     "Body is null"
                                 else
                                     "Successful"
            in 400..499 -> when (code) {
                                     HTTP_CONNECTION_FAILED -> "Connection failed"
                                     HTTP_NOT_FOUND -> "Not found"
                                     HTTP_FORBIDDEN -> "Forbidden"
                                     else -> "Bad request"
                                 }
            in 500..599 -> if (code == HTTP_UNKNOWN_ERROR)
                                     "Unknown error"
                                 else
                                     "Internal server error"
            else -> "Continue..."
        }
    }
    val exceptionCheck = { e: Exception ->
        when (e) {
            is HttpException -> e.code()
            is ConnectException -> HTTP_CONNECTION_FAILED
            else -> HTTP_UNKNOWN_ERROR
        }
    }
    enum class Type {
        AVATAR,
        IMAGE,
        VIDEO
    }

    fun showToastAfterSave(
        fragmentContext: Context?,
        viewContext: Context,
        postId: Long?,
        initialContent: String?,
        newContent: String?
    ) {
        if (newContent != initialContent)
            if (!newContent.isNullOrBlank())
                Toast.makeText(
                    fragmentContext,
                    viewContext.getString(
                            if (postId == 0L)
                                R.string.new_post_has_created
                            else
                                R.string.post_has_edited
                    ),
                    Toast.LENGTH_LONG
                ).show()
    }

    fun customLog(action: String, e: Exception) {
        Log.d(action, "CAUGHT EXCEPTION => $e\n" +
                "DESCRIPTION => ${overview(exceptionCheck(e))}")
    }

    fun listToString(list: List<Any>) =
        "${list.map {
            when (it) {
                is Post -> "id = ${it.id}, idFromServer = ${it.idFromServer}"
                is PostEntity -> "id = ${it.id}, idFromServer = ${it.idFromServer}"
                else -> return "Can't cast to known class"
            }
        }}"

    fun CombinedLoadStates.allStatesToString(): String =
        "CombinedLoadStates:\nrefresh = $refresh,\nprepend = $prepend,\nappend = $append;\n" +
                "source.refresh = ${source.refresh},\nsource.prepend = ${source.prepend},\nsource.append = ${source.append};\n" +
                "mediator.refresh = ${mediator?.refresh},\nmediator.prepend = ${mediator?.prepend},\nmediator.append = ${mediator?.append}."

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