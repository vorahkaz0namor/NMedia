package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.ViewModelFactory

class AuthDialogFragment : DialogFragment() {
    private val dependencyContainer = DependencyContainer.getInstance()
    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireActivity,
        factoryProducer = {
            ViewModelFactory(
                dependencyContainer.postRepository,
                dependencyContainer.authRepository,
                dependencyContainer.appAuth
            )
        }
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .let {
                if (authViewModel.authorized) {
                    it.setTitle(getString(R.string.make_logout))
                    it.setPositiveButton(getString(R.string.item_logout)) { _, _ ->
                        authViewModel.logout()
                        findNavController().navigate(R.id.feedFragment)
                    }
                } else {
                    it.setTitle(getString(R.string.must_to_login))
                    it.setMessage(getString(R.string.wish_to_login))
                    it.setPositiveButton(getString(R.string.item_login)) { _, _ ->
                        authViewModel.authShowing()
                        findNavController().navigate(R.id.loginFragment)
                        this.dismiss()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

    companion object {
        const val AUTH_TAG = "AuthenticationDialog"
    }
}