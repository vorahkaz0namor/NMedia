package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityAttachmentsBinding

class AttachmentsActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAttachmentsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        loadIntent()
        setupListeners()
    }

    private fun loadIntent() {
        intent.let {
            val content = it.getStringExtra(Intent.EXTRA_TEXT)
            val preview = it.getIntExtra(PREVIEW_KEY, 0)
            binding.apply {
                if (preview != 0)
                    attachmentPreview.setImageResource(preview)
                postContent.text = content
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            sendUrlToIntent(play, attachmentPreview)
        }
    }

    private fun sendUrlToIntent(vararg view: View) {
        val url = intent.getStringExtra(Intent.EXTRA_ORIGINATING_URI)
        view.forEach {
            it.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }
}