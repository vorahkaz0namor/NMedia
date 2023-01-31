package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import okhttp3.internal.http.HTTP_OK
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.CompanionNotMedia
import ru.netology.nmedia.util.CompanionNotMedia.POST_CONTENT
import ru.netology.nmedia.util.CompanionNotMedia.overview
import ru.netology.nmedia.util.CompanionNotMedia.showToastAfterSave
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment(R.layout.fragment_new_post) {
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private var _binding: FragmentNewPostBinding? = null
    private val binding: FragmentNewPostBinding
        get() = _binding!!
    private var snackbar: Snackbar? = null
    private var savedPostId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Сохранение черновика
//            viewModel.saveDraftCopy(binding.newContent.text.toString())
            findNavController().navigateUp()
        }
    }

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
        subscribe()
    }

    private fun initView() {
        binding.newContent.apply {
            // Загрузка переданного на редактирование content'а,
            // или загрузка черновика, если он был сохранен
            setText(arguments?.POST_CONTENT /*?: viewModel.getDraftCopy()*/)
            requestFocus()
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
                    // Изменение состояния отображения, пока не закончится
                    // уже запущенный процесс сохранения
                    AndroidUtils.hideKeyboard(newContent)
                    newPostGroup.isVisible = false
                    progressBarView.progressBar.isVisible = true
                    savedPostId = viewModel.savePost(newContent.text.toString())
                }
            }
            cancelEdit.setOnClickListener {
                findNavController().navigateUp().also {
//                    viewModel.saveDraftCopy(null)
                }
            }
        }
    }

    private fun subscribe() {
        viewModel.postEvent.observe(viewLifecycleOwner) {
            viewModel.loadPosts()
            showToastAfterSave(
                context,
                binding.root.context,
                savedPostId,
                arguments?.POST_CONTENT,
                binding.newContent.text.toString(),
                viewModel.postEvent.value!!
            )
            findNavController().navigateUp().also {
                // Очистка черновика
//                viewModel.saveDraftCopy(null)
            }
        }
    }
}