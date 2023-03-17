package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentAttachmentsBinding
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.util.*
import ru.netology.nmedia.util.CompanionNotMedia.load
import ru.netology.nmedia.util.CompanionNotMedia.Type
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.ViewModelFactory

class AttachmentsFragment : Fragment(R.layout.fragment_attachments) {
    private val dependencyContainer = DependencyContainer.getInstance()
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
        factoryProducer = {
            ViewModelFactory(
                dependencyContainer.postRepository,
                dependencyContainer.authRepository,
                dependencyContainer.appAuth
            )
        }
    )
    private val binding by viewBinding(FragmentAttachmentsBinding::bind)
    private lateinit var attachment: Attachment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            customNavigateUp()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadIntent()
        setupListeners()
    }

    private fun loadIntent() {
        attachment = viewModel.viewingAttachments.value?.attachment!!
        binding.apply {
            attachmentPreview.load(
                url = viewModel.getAttachmentUrl(attachment.url),
                type = attachment.type.name
            )
            play.isVisible = (attachment.type.name == Type.VIDEO.name)
            attachmentDescription.isVisible = (attachment.description != null)
            attachmentDescription.text = attachment.description ?: ""
        }
    }

    private fun setupListeners() {
        binding.apply {
            if (attachment.type.name == Type.VIDEO.name)
                sendUrlToIntent(play, attachmentPreview)
            finishView.setOnClickListener {
                customNavigateUp()
            }
        }
    }

    private fun sendUrlToIntent(vararg view: View) {
        view.forEach {
            it.setOnClickListener {
                startActivity(Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(viewModel.getAttachmentUrl(attachment.url))
                ))
            }
        }
    }

    private fun customNavigateUp() {
        viewModel.clearAttachments()
        viewModel.loadPosts()
        findNavController().navigateUp()
    }
}