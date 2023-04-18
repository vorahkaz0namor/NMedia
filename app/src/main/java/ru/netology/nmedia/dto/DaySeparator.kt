package ru.netology.nmedia.dto

import androidx.annotation.StringRes
import ru.netology.nmedia.R

enum class DaySeparator(@StringRes val dayName: Int) {
    TODAY(R.string.today),
    YESTERDAY(R.string.yesterday),
    THIS_WEEK(R.string.this_week),
    LAST_WEEK(R.string.last_week),
    LONG_TIME_AGO(R.string.long_time_ago)
}