package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import androidx.work.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.worker.SendPushTokenWorker

// Чтобы невозможно было создать несколько объектов данного типа,
// вместе с использованием companion object следует использовать
// приватный конструктор
class AppAuth(
    private val context: Context,
    private val workManager: WorkManager
) {
    companion object {
        private const val TOKEN_KEY = "TOKEN_KEY"
        private const val ID_KEY = "ID_KEY"
    }
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _data: MutableStateFlow<AuthModel?>

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0L)
        if (token == null || id == 0L) {
            _data = MutableStateFlow(null)
            // Для осуществления действий с SharedPreferences существует
            // конечно метод edit(), но предпочтительнее пользоваться
            // расширением edit{}, который по окончании всех действий
            // автоматически вызывает commit().
            prefs.edit { clear() }
        } else
            _data = MutableStateFlow(value = AuthModel(id, token))
        // После реализации WorkManager'а можно не отправлять токен при
        // инициализации данного объекта
//        sendPushToken()
    }

    val data = _data.asStateFlow()

    // Для перестраховки можно указать аннотацию @Synchronized
    @Synchronized
    fun setAuth(authModel: AuthModel) {
        _data.value = authModel
        prefs.edit {
            putLong(ID_KEY, authModel.id)
            putString(TOKEN_KEY, authModel.token)
        }
        sendPushToken()
    }

    @Synchronized
    fun removeAuth() {
        _data.value = null
        prefs.edit { clear() }
        sendPushToken()
    }

    // После перехода на WorkManager данная функция будет вызывать
    // соответствующий Worker
    fun sendPushToken(token: String? = null) {
        // Создается запрос с указанием типа Worker'а
        val request = OneTimeWorkRequestBuilder<SendPushTokenWorker>()
            // Для данного случая устанавливаются ограничения на Интернет
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            // И передается токен для отправки
            .setInputData(
                Data.Builder()
                    .putString(SendPushTokenWorker.TOKEN_KEY, token)
                    .build()
            )
            .build()
        // По умолчанию WorkManager конфигурируется автоматически при
        // запуске приложения
        workManager.beginUniqueWork(
            SendPushTokenWorker.NAME,
            // При передаче нового токена предыдущий будет заменен
            // в запросе на новый
            ExistingWorkPolicy.REPLACE,
            request
        ).enqueue()
    }
}