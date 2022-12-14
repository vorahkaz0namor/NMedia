package ru.netology.nmedia.util

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.Toast
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
}