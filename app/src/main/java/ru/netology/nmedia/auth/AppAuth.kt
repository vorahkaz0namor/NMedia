package ru.netology.nmedia.auth

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.util.CompanionNotMedia.exceptionCheck
import ru.netology.nmedia.util.CompanionNotMedia.overview
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context,
//    private val firebaseMessaging: FirebaseMessaging
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

    // В данном случае предоставление доступа к ApiService осуществляется
    // достаточно нестандартным способом
    // Для этого создается интерфейс
    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun getPostApiService(): PostApiService
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            // Чтобы получить объект с аннотацией @EntryPoint, необходимо
            // использовать класс EntryPointAccessors
            val entryPoint = EntryPointAccessors.fromApplication(
                context,
                AppAuthEntryPoint::class.java
            )
            try {
                val pushToken = PushToken(
                    token ?: Firebase.messaging.token.await()
                )
                entryPoint.getPostApiService().sendPushToken(pushToken)
            } catch (e: Exception) {
                Log.d("SENDING TOKEN", "CAUGHT EXCEPTION => $e\n" +
                        "DESCRIPTION => ${overview(exceptionCheck(e))}")
            }
        }
    }
}