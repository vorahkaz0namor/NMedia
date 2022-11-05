package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityNewPostBinding

class NewPostActivity : AppCompatActivity() {
    private val binding by lazy { ActivityNewPostBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initialView()
        setupListeners()
    }

    private fun initialView() {
        binding.apply {
            newContent.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
            newContent.requestFocus()
        }
    }

    private fun setupListeners() {
        binding.apply {
            saveThisPost.setOnClickListener {
                val intent = Intent()
                if (newContent.text.isNullOrBlank())
                    Snackbar.make(
                        root,
                        R.string.empty_content,
                        BaseTransientBottomBar.LENGTH_INDEFINITE
                    ).setAction(android.R.string.ok) {}.show()
                else {
                    val content = newContent.text.toString()
                    intent.putExtra(Intent.EXTRA_TEXT, content)
                    setResult(Activity.RESULT_OK, intent)
                    // work ending of Activity
                    finish()
                }
            }
            cancelEdit.setOnClickListener {
                setResult(Activity.RESULT_CANCELED, intent)
                finish()
            }
        }
    }
}