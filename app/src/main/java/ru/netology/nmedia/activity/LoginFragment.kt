package ru.netology.nmedia.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
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
    private val authViewModel: AuthViewModel by activityViewModels()
    private val binding by viewBinding(LoginLayoutBinding::bind)
    private lateinit var avatarLauncher: ActivityResultLauncher<Intent>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setupListeners()
        subscribe()
    }

    override fun onStop() {
        super.onStop()
        AndroidUtils.hideKeyboard(binding.root)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        authViewModel.clearAvatar()
    }

    private fun initView() {
        avatarLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> Snackbar.make(
                        binding.root,
                        getString(R.string.error_load_photo),
                        Snackbar.LENGTH_LONG
                    )
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .show()
                    else -> {
                        val uri = it.data?.data ?: return@registerForActivityResult
                        authViewModel.addAvatar(uri, uri.toFile())
                    }
                }
            }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        else {
            dialog?.window?.setDecorFitsSystemWindows(false)
            binding.root.onApplyWindowInsets(WindowInsets.CONSUMED)
        }
    }

    private fun setupListeners() {
        binding.apply {
            avatarImage.setOnClickListener {
                ImagePicker.with(this@LoginFragment)
                    .galleryOnly()
                    .crop()
                    .createIntent {
                        avatarLauncher.launch(it)
                    }
            }
            clearAvatar.setOnClickListener {
                authViewModel.clearAvatar()
            }
            loginButton.setOnClickListener {
                AndroidUtils.hideKeyboard(this.root)
                if (textValidation())
                    authViewModel.login(
                        login = loginField.editText?.text.toString(),
                        password = passwordField.editText?.text.toString()
                    )
            }
            regButton.setOnClickListener {
                AndroidUtils.hideKeyboard(this.root)
                avatarPreviewGroup.isVisible = false
                if (textValidation() &&
                    passwordField.editText?.text
                        .contentEquals(confirmPasswordField.editText?.text)
                )
                    authViewModel.register(
                        name =
                        if (!nameField.editText?.text.isNullOrBlank())
                            nameField.editText?.text.toString()
                        else
                            "User",
                        login = loginField.editText?.text.toString(),
                        password = passwordField.editText?.text.toString()
                    )
            }
            cancelButton.setOnClickListener {
                customNavigateUp()
            }
        }
    }

    private fun subscribe() {
        binding.apply {
            setInvisibleErrorWrongLoginPassword(loginField.editText, passwordField.editText)
            if (authViewModel.authState.value?.regShowing == true)
                setInvisibleErrorPasswordsDontMatch(passwordField.editText, confirmPasswordField.editText)
        }
        authViewModel.apply {
            authState.observe(viewLifecycleOwner) { state ->
                binding.apply {
                    progressBarView.progressBar.isVisible = state.loading
                    authView.isVisible = state.authShowing
                    regView.isVisible = state.regShowing
                    commonView.isVisible = state.authShowing || state.regShowing
                }
            }
            media.observe(viewLifecycleOwner) { avatar ->
                binding.apply {
                    if (avatar != null)
                        avatarImage.isVisible = false
                    avatarPreviewGroup.isVisible = (avatar != null)
                    avatarPreview.setImageURI(avatar?.uri)
                }
            }
            authEvent.observe(viewLifecycleOwner) { code ->
                if (code == HTTP_OK) {
                    if (authState.value?.regShowing == true)
                        Toast.makeText(
                            context,
                            getString(R.string.successful_regin),
                            Toast.LENGTH_LONG
                        ).show()
                    customNavigateUp()
                }
                else {
                    val condition = (authState.value?.authShowing == true &&
                                     code == HTTP_BAD_REQUEST || code == HTTP_NOT_FOUND)
                    binding.wrongLoginPassword.isVisible = condition
                    if (!condition)
                        Snackbar.make(
                            binding.root,
                            overview(code),
                            Snackbar.LENGTH_INDEFINITE
                        )
                            .setAction(android.R.string.ok) {
                                saveAuthError(code)
                                customNavigateUp()
                            }
                            .show()
                }
            }
        }
    }

    private fun textValidation() = (
        !binding.loginField.editText?.text.isNullOrBlank() &&
        !binding.passwordField.editText?.text.isNullOrBlank()
    )

    private fun setInvisibleErrorWrongLoginPassword(vararg text: EditText?) =
        text.map {
            it?.addTextChangedListener {
                binding.wrongLoginPassword.isVisible = false
            }
        }

    private fun setInvisibleErrorPasswordsDontMatch(vararg text: EditText?) =
        text.map {
            it?.addTextChangedListener { field ->
                binding.passwordsDontMatch.isVisible =
                    (field?.contentEquals(binding.passwordField.editText?.text) == false ||
                            !field.contentEquals(binding.confirmPasswordField.editText?.text))
            }
        }

    private fun customNavigateUp() {
        authViewModel.clearAvatar()
        findNavController().navigateUp()
    }
}