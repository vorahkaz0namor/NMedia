package ru.netology.nmedia.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.LoginLayoutBinding
import ru.netology.nmedia.util.viewBinding
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

class AuthDialogFragment : DialogFragment() {
    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireActivity
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.make_logout))
            .setPositiveButton(getString(R.string.item_logout)) { _, _ ->
                authViewModel.logout()
                findNavController().navigate(R.id.feedFragment)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

    companion object {
        const val AUTH_TAG = "AuthenticationDialog"
    }
}