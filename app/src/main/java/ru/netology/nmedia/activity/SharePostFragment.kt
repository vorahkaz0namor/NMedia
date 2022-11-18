package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSharePostBinding
import ru.netology.nmedia.util.CompanionNotMedia.POST_CONTENT

class SharePostFragment : Fragment(R.layout.fragment_share_post) {
    private var _binding: FragmentSharePostBinding? = null
    private val binding: FragmentSharePostBinding
        get() = _binding!!
    private lateinit var snackbar: Snackbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSharePostBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (snackbar.isShown)
            snackbar.dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadIntent()
    }

    private fun loadIntent() {
                val text = arguments?.POST_CONTENT
                snackbar = Snackbar.make(
                    binding.root,
                    if (text.isNullOrBlank())
                        R.string.error_empty_content
                    else {
                        binding.postText.text = text
                        R.string.complete_sharing
                    },
                    BaseTransientBottomBar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    findNavController().navigateUp()
                }
                snackbar.show()
    }
}