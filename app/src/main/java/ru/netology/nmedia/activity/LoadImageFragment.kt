package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentLoadImageBinding
import ru.netology.nmedia.util.CompanionNotMedia.load
import ru.netology.nmedia.util.WorkerThread
import ru.netology.nmedia.util.viewBinding

class LoadImageFragment : Fragment(R.layout.fragment_load_image) {
    private val binding by viewBinding(FragmentLoadImageBinding::bind)
    // Поскольку запуск происходит не сразу, а с некоторой задержкой,
    // поэтому можно сразу запускать создаваемый поток
    private val worker = WorkerThread().apply { start() }
    private val urls = listOf(
        "netology.jpg",
        "sber.jpg",
        "tcs.jpg",
        "avatar1.jpg",
        "avatar2.jpg",
        "Sneg.jpg",
        "localuser.jpg",
        "404.jpg"
    )
    private var index = 0
    companion object {
        private const val BASE_URL = "http://192.168.31.16:9999"
        private const val PATH = "/avatars/"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        showLoadedImage()
        setupListeners()
    }

    private fun setupListeners() {
        binding.loadButton.setOnClickListener {
            if (index == urls.size)
                index = 0
            val url = "$BASE_URL$PATH${urls[index++]}"
            // С использованием класса WorkerThread
//            worker.download(url)
            // С использованием библиотеки Glide
            binding.image.load(url)
        }
    }

    private fun showLoadedImage() {
        // Сюда прилетает полученная картинка, и теперь ее можно отобразить
        worker.resultCallback = {
            binding.image.setImageBitmap(it)
        }
    }
}