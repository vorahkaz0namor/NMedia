package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.http.HTTP_CONTINUE
import okhttp3.internal.http.HTTP_OK
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.model.AuthModelState
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.repository.AuthRepository
import ru.netology.nmedia.repository.AuthRepositoryImpl
import ru.netology.nmedia.util.CompanionNotMedia.exceptionCheck
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File

class AuthViewModel : ViewModel() {
    private val repository: AuthRepository = AuthRepositoryImpl()
    private val _authEvent = SingleLiveEvent(HTTP_CONTINUE)
    val authEvent: LiveData<Int>
        get() = _authEvent
    private val _authState = MutableLiveData(AuthModelState())
    val authState: LiveData<AuthModelState>
        get() = _authState
    private val _media = MutableLiveData<MediaModel?>(null)
    val media: LiveData<MediaModel?>
        get() = _media
    val data: LiveData<AuthModel?> =
        AppAuth.getInstance().data.asLiveData(Dispatchers.Default)
    val authorized: Boolean
        get() = data.value != null
    val checkAuthorized = MutableLiveData(false)
    val authError = MutableLiveData(HTTP_OK)

    fun login(login: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value?.loading()
                repository.login(login, password)
                _authEvent.value = HTTP_OK
            } catch (e: Exception) {
                _authEvent.value = exceptionCheck(e)
                _authState.value = _authState.value?.error()
            }
        }
    }

    fun register(name: String, login: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value?.loading()
                repository.register(name, login, password, media.value)
                _authState.value = _authState.value?.regShowing()
                _authEvent.value = HTTP_OK
            } catch (e: Exception) {
                _authEvent.value = exceptionCheck(e)
                _authState.value = _authState.value?.error()
            }
        }
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
    }

    fun checkAuth() {
        viewModelScope.launch {
            checkAuthorized.value = true
            checkAuthorized.value = false
        }
    }

    fun saveAuthError(code: Int) {
        viewModelScope.launch { authError.value = code }
    }

    fun clearAuthError() {
        viewModelScope.launch {
            authError.value = HTTP_OK
            _authState.value = _authState.value?.authShowing()
        }
    }

    fun addAvatar(uri: Uri, file: File) {
        viewModelScope.launch {
            _media.value = MediaModel(uri, file)
        }
    }

    fun clearAvatar() {
        viewModelScope.launch {
            _media.value = null
        }
    }

    fun authShowing() {
        viewModelScope.launch { _authState.value = _authState.value?.authShowing() }
    }

    fun regShowing() {
        viewModelScope.launch { _authState.value = _authState.value?.regShowing() }
    }
}