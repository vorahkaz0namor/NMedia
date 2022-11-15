package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.CompanionNotMedia.POST_CONTENT
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment(R.layout.fragment_new_post) {
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private var _binding: FragmentNewPostBinding? = null
    private val binding: FragmentNewPostBinding
        get() = _binding!!
    private var snackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPostBinding.inflate(inflater)
        return binding.root
    }

    override fun onStop() {
        super.onStop()
        if (snackbar != null && snackbar?.isShown == true)
            snackbar?.dismiss()
        viewModel.clearEditedValue()
        AndroidUtils.hideKeyboard(binding.newContent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setupListeners()
    }

    private fun initView() {
        binding.apply {
            newContent.setText(arguments?.POST_CONTENT)
            newContent.requestFocus()
        }
    }

    private fun setupListeners() {
        binding.apply {
            saveThisPost.setOnClickListener {
                if (newContent.text.isNullOrBlank()) {
                    snackbar = Snackbar.make(
                                   root,
                                   R.string.empty_content,
                                   BaseTransientBottomBar.LENGTH_INDEFINITE
                               ).setAction(android.R.string.ok) {}
                    snackbar?.show()
                }
                else {
                    addingNewPost(newContent.text.toString())
                    findNavController().navigateUp()
                }
            }
            cancelEdit.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun addingNewPost(content: String?) {
        val initialContent = viewModel.edited.value?.content
        val postId = viewModel.savePost(content)
        if (content != initialContent)
            if (!content.isNullOrBlank())
                Toast.makeText(
                    this.context,
                    binding.root.context.getString(
                        if (postId == 0L)
                            R.string.new_post_has_created
                        else
                            R.string.post_has_edited
                    ),
                    Toast.LENGTH_LONG
                ).show()
    }
}