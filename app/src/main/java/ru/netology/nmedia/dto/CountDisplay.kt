package ru.netology.nmedia.dto

import android.annotation.SuppressLint
import android.util.Log
import ru.netology.nmedia.util.CompanionNotMedia.actualTime
import ru.netology.nmedia.util.CompanionNotMedia.epochMultiplier
import java.math.BigDecimal
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

    fun daySeparator(previousPost: Post?, currentPost: Post): String? {
        val coefficient = epochMultiplier(currentPost.published)
        val oneDayLength = 86_400_000L / coefficient
        val oneWeekLength = 7 * oneDayLength
        val twoWeekLength = 2 * oneWeekLength
        // Поскольку метод java.util.Date().day, который определяет день недели,
        // является deprecated и похоже уже не работает, а метод
        // OffsetDateTime.now().dayOfWeek требует либо понять версию API,
        // либо использовать другие обходные пути, то я выбрал не совсем "хороший"
        // вариант решения данной проблемы - определение и задание "вручную" самого
        // первого понедельника в Unix Time
        val firstMondayUnixTime = 4 * oneDayLength
        val localeShift = 25_200_000L / coefficient
        val currentTime = System.currentTimeMillis() / coefficient
        val floorCurrentTimeToDay = (currentTime + localeShift) / oneDayLength
        val floorCurrentTimeToWeek = (currentTime - firstMondayUnixTime) / oneWeekLength
        val currentDayStart = oneDayLength * floorCurrentTimeToDay - localeShift
        val currentWeekStart = oneWeekLength * floorCurrentTimeToWeek + firstMondayUnixTime - localeShift
        val dayOfPost = { post: Post ->
            when (post.published) {
                in currentDayStart..currentTime ->
                    DaySeparator.TODAY.dayName
                in currentDayStart - oneDayLength until currentDayStart ->
                    DaySeparator.YESTERDAY.dayName
                in currentWeekStart until  currentDayStart - oneDayLength ->
                    DaySeparator.THIS_WEEK.dayName
                in currentWeekStart - oneWeekLength until currentWeekStart ->
                    DaySeparator.LAST_WEEK.dayName
                in currentWeekStart - twoWeekLength until currentWeekStart - oneWeekLength ->
                    DaySeparator.TWO_WEEKS_AGO.dayName
                in Long.MIN_VALUE until currentWeekStart - twoWeekLength ->
                    DaySeparator.LONG_TIME_AGO.dayName
                else ->
                    DaySeparator.TOMORROW.dayName
            }
        }
        val dayOfPreviousPost = previousPost?.let { dayOfPost(it) }
        val dayOfCurrentPost = dayOfPost(currentPost)
//        val result = dayOfCurrentPost.takeIf { it != dayOfPreviousPost }
//        Log.d("DAY OF THE POST", "currentTime = ${actualTime(currentTime)}\n" +
//                "floorTime = $floorCurrentTimeToDay\n" +
//                "floorWeek = $floorCurrentTimeToWeek\n" +
//                "dayStart = ${actualTime(currentDayStart)}\n" +
//                "weekStart = ${actualTime(currentWeekStart)}\n" +
//                "previous = ${actualTime(previousPost?.published ?: 1)}, $dayOfPreviousPost\n" +
//                "current = ${actualTime(currentPost.published)}, $dayOfCurrentPost\n" +
//                "separator = $result")
        return dayOfCurrentPost.takeIf { it != dayOfPreviousPost }
    }
}