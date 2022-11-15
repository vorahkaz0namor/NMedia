package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentAttachmentsBinding
import ru.netology.nmedia.util.CompanionNotMedia.ATTACHMENT_PREVIEW
import ru.netology.nmedia.util.CompanionNotMedia.ATTACHMENT_URI
import ru.netology.nmedia.util.CompanionNotMedia.POST_CONTENT
import ru.netology.nmedia.util.*

class AttachmentsFragment : Fragment(R.layout.fragment_attachments) {
    private val binding by viewBinding(FragmentAttachmentsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadIntent()
        setupListeners()
    }

    private fun loadIntent() {
        val preview = arguments?.ATTACHMENT_PREVIEW
        binding.apply {
            if (preview != null)
                attachmentPreview.setImageResource(preview.toInt())
            postContent.text = arguments?.POST_CONTENT ?: ""
        }
    }

    private fun setupListeners() {
        binding.apply {
            sendUrlToIntent(play, attachmentPreview)
            finishView.setOnClickListener { findNavController().navigateUp() }
        }
    }

    private fun sendUrlToIntent(vararg view: View) {
        view.forEach {
            it.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(arguments?.ATTACHMENT_URI)))
            }
        }
    }
}