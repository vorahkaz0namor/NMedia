package ru.netology.nmedia.dto

enum class DaySeparator(val dayName: String) {
    TODAY("Сегодня"),
    YESTERDAY("Вчера"),
    THIS_WEEK("На этой неделе"),
    LAST_WEEK("На прошлой неделе"),
    TWO_WEEKS_AGO("Две недели назад"),
    LONG_TIME_AGO("Совсем давно"),
    TOMORROW("Завтра")
}