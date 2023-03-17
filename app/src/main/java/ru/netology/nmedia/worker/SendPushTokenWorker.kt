package ru.netology.nmedia.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.util.CompanionNotMedia.exceptionCheck
import ru.netology.nmedia.util.CompanionNotMedia.overview

// WorkManager позволяет выполнить какое-либо действие, которое обязательно
// надо выполнить. При этом если происходится ошибка и действие не завершается
// успехом, то это действие будет повторяться до тех пор, пока не завершиться
// успехом.
// WorkManager накладывает ограничение на частоту выполнения отложенной
// задачи - это 15 минут. Если пытаться отправлять запрос чаще этого
// времени, то такой запрос не будет отрабатываться
class SendPushTokenWorker(
    // По умолчанию все будет через рефлексию создаваться
    // и эти параметры будут самой библиотекой сюда передаваться
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    companion object {
        // В данный класс тоже можно передать существующий токен,
        // либо сгенерировать его внутри. Поэтому создается ключ,
        // по которому можно передать токен
        const val TOKEN_KEY = "TOKEN_KEY"
        // Также нужно задать имя для данного Worker'а
        const val NAME = "SendPushToken"
    }
    override suspend fun doWork(): Result {
        val pushToken = PushToken(
            // А здесь можно вынуть переданный токен
            inputData.getString(TOKEN_KEY) ?: Firebase.messaging.token.await()
        )
        return try {
            DependencyContainer.getInstance().postApiService.sendPushToken(pushToken)
            Result.success()
        } catch (e: Exception) {
            Log.d( "SENDING TOKEN","CAUGHT EXCEPTION => $e\n" +
                    "DESCRIPTION => ${overview(exceptionCheck(e))}")
            Result.retry()
        }
    }
}