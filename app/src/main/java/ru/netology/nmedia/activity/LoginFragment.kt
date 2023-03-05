package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import okhttp3.internal.http.HTTP_BAD_REQUEST
import okhttp3.internal.http.HTTP_NOT_FOUND
import okhttp3.internal.http.HTTP_OK
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.LoginLayoutBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.CompanionNotMedia.overview
import ru.netology.nmedia.util.viewBinding
import ru.netology.nmedia.viewmodel.AuthViewModel

class LoginFragment : DialogFragment(R.layout.login_layout) {
    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireActivity
    )
    private val binding by viewBinding(LoginLayoutBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        subscribe()
    }

    override fun onStop() {
        super.onStop()
        AndroidUtils.hideKeyboard(binding.root)
        binding.wrongLoginPassword.isVisible = false
    }

    private fun setupListeners() {
        binding.apply {
            loginButton.setOnClickListener {
                if (textValidation()) {
                    AndroidUtils.hideKeyboard(this.root)
                    authViewModel.login(
                        login = loginField.editText?.text.toString(),
                        password = passwordField.editText?.text.toString()
                    )
                }
            }
            cancelButton.setOnClickListener {
                this@LoginFragment.dismiss()
            }
        }
    }

    private fun subscribe() {
        binding.apply { setInvisibleError(loginField.editText, passwordField.editText) }
        authViewModel.apply {
            authState.observe(viewLifecycleOwner) { state ->
                binding.apply {
                    progressBarView.progressBar.isVisible = state.loading
                    authView.isVisible = state.showing
                }
            }
            authEvent.observe(viewLifecycleOwner) { code ->
                if (code == HTTP_OK)
                    this@LoginFragment.dismiss()
                else {
                    val condition = (code == HTTP_BAD_REQUEST || code == HTTP_NOT_FOUND)
                    binding.wrongLoginPassword.isVisible = condition
                    if (!condition)
                        Snackbar.make(
                            binding.root,
                            overview(code),
                            Snackbar.LENGTH_INDEFINITE
                        )
                            .setAction(android.R.string.ok) {
                                saveAuthError(code)
                                this@LoginFragment.dismiss()
                            }
                            .show()
                }
            }
        }
    }

    private fun textValidation() =
        (!binding.loginField.editText?.text.isNullOrBlank() &&
                !binding.passwordField.editText?.text.isNullOrBlank())

    private fun setInvisibleError(vararg text: EditText?) =
        text.map {
            it?.addTextChangedListener {
                binding.wrongLoginPassword.isVisible = false
            }
        }

    companion object {
        const val LOGIN_TAG = "AuthenticationFragment"
    }
}