package ru.netology.nmedia.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityIntentHandlerBinding

class IntentHandlerActivity : AppCompatActivity() {
    private val binding by lazy { ActivityIntentHandlerBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        loadIntent()
    }

    private fun loadIntent() {
        intent?.let {
            if (it.action == Intent.ACTION_SEND) {
                val text = it.getStringExtra(Intent.EXTRA_TEXT)
                Snackbar.make(
                    binding.root,
                    if (text.isNullOrBlank())
                        R.string.error_empty_content
                    else {
                        binding.postText.text = text
                        R.string.complete_sharing
                    },
                    LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    // work ending of Activity
                    finish()
                }.show()
            }
        }
    }
}