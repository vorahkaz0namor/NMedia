package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.util.CompanionNotMedia.exceptionCheck
import ru.netology.nmedia.util.CompanionNotMedia.overview

// Чтобы невозможно было создать несколько объектов данного типа,
// вместе с использованием companion object следует использовать
// приватный конструктор
class AppAuth private constructor(context: Context) {
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
        sendPushToken()
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

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            val pushToken = PushToken(token ?: Firebase.messaging.token.await())
            // FirebaseMessaging.getInstance().token.await()
            try {
                PostApi.service.sendPushToken(pushToken)
            } catch (e: Exception) {
                println("CAUGHT EXCEPTION WHEN SEND TOKEN => $e\n" +
                        "DESCRIPTION => ${overview(exceptionCheck(e))}\n")
            }
        }
    }

    companion object {
        @Volatile
        private var instance: AppAuth? = null
        private const val TOKEN_KEY = "TOKEN_KEY"
        private const val ID_KEY = "ID_KEY"

        fun getInstance(): AppAuth = synchronized(this) {
            requireNotNull(instance) {
                "You must call init(context: Context) first!"
            }
        }

        fun init(context: Context): AppAuth = synchronized(this) {
            instance ?: AppAuth(context).apply { instance = this }
        }
    }
}