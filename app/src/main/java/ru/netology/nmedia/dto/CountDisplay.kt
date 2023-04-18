package ru.netology.nmedia.dto

import android.util.Log
import ru.netology.nmedia.util.CompanionNotMedia.formatNMedia
import ru.netology.nmedia.util.CompanionNotMedia.timeInHumanRepresentation
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.Instant
import java.time.OffsetDateTime

object CountDisplay {
    fun show(count: Int): String {
        if (count < 0) return "0"
        if (count < 1_000) return count.toString()
        val countBigDecimal = BigDecimal(count)
        val digitsMap = mapOf(Pair(1, ""), Pair(1_000, "K"), Pair(1_000_000, "M"))

        val divisor = digitsMap.keys.elementAt(
            if (count >= digitsMap.keys.elementAt(1))
                if (count >= digitsMap.keys.elementAt(2))
                    2
                else 1
            else 0
        )

        return "${countBigDecimal.divide(
            BigDecimal(divisor),
            if (count % divisor == 0 ||
                count % divisor < 100 ||
                count / divisor >= 10) 0 else 1,
            BigDecimal.ROUND_DOWN
        )}${digitsMap.getValue(divisor)}"
    }

    fun daySeparator(previousPost: Post?, currentPost: Post): Int? {
        val today = OffsetDateTime.now()
        val yesterday = today.minusDays(1)
        var dayOfCurrentWeekStart = today
        while (dayOfCurrentWeekStart.dayOfWeek != DayOfWeek.MONDAY)
            dayOfCurrentWeekStart = dayOfCurrentWeekStart.minusDays(1)
        val dayOfLastWeekStart = dayOfCurrentWeekStart.minusWeeks(1).dayOfYear
        val dayOfPost = { post: Post ->
            when (
                Instant
                    .ofEpochSecond(post.published)
                    .atOffset(today.offset)
                    .dayOfYear
            ) {
                today.dayOfYear ->
                    DaySeparator.TODAY.dayName
                yesterday.dayOfYear ->
                    DaySeparator.YESTERDAY.dayName
                in dayOfCurrentWeekStart.dayOfYear until yesterday.dayOfYear ->
                    DaySeparator.THIS_WEEK.dayName
                in dayOfLastWeekStart until dayOfCurrentWeekStart.dayOfYear ->
                    DaySeparator.LAST_WEEK.dayName
                else ->
                    DaySeparator.LONG_TIME_AGO.dayName
            }
        }
        val dayOfPreviousPost = previousPost?.let { dayOfPost(it) }
        val dayOfCurrentPost = dayOfPost(currentPost)
//        val result = dayOfCurrentPost.takeIf { it != dayOfPreviousPost }
//        Log.d("DAY OF THE POST", "today = ${formatNMedia(today)}\n" +
//                "yesterday = ${formatNMedia(yesterday)}\n" +
//                "dayOfCurrentWeekStart = ${formatNMedia(dayOfCurrentWeekStart)}\n" +
//                "dayOfLastWeekStart.dayOfYear = $dayOfLastWeekStart\n" +
//                "previousPost = ${previousPost?.let { timeInHumanRepresentation(it.published)}}, $dayOfPreviousPost\n" +
//                "currentPost = ${timeInHumanRepresentation(currentPost.published)}, $dayOfCurrentPost\n" +
//                "daySeparator = $result")
        return dayOfCurrentPost.takeIf { it != dayOfPreviousPost }
    }
}