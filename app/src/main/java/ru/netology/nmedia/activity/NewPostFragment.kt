package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import okhttp3.internal.http.HTTP_OK
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
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
    private lateinit var photoLauncher: ActivityResultLauncher<Intent>
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Сохранение черновика
//            viewModel.saveDraftCopy(binding.newContent.text.toString())
            customNavigateUp()
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
        photoLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> Snackbar.make(
                            binding.root,
                            getString(R.string.error_load_photo),
                            Snackbar.LENGTH_LONG
                        )
                            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                            .show()
                    else -> {
                        val uri = it.data?.data ?: return@registerForActivityResult
                        // Функция .toFile() на основе URI определяет, где расположен файл
                        // и таким образом представляет URI в виде файла. Но эта функция
                        // работает только если URI указывает на локальный ресурс, иначе
                        // будет исключение.
                        viewModel.changePhoto(uri.toFile(), uri)
                    }
                }
            }
    }

    private fun setupListeners() {
        binding.apply {
            // Современный подход по созданию меню (2023 год).
            // Здесь используется интерфейс addMenuProvider,
            // который содержит в себе всю логику по созданию
            // меню и обратботке кликов.
            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.new_post_menu, menu)
                }
                override fun onMenuItemSelected(menuItem: MenuItem) =
                    when (menuItem.itemId) {
                        R.id.new_post -> {
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
                                viewModel.savePost(newContent.text.toString())
                            }
                            true
                        }
                        else -> false
                    }
            // Рекомендуется указывать LifecycleOwner (в случае с фрагментом -
            // viewLifecycleOwner), чтобы когда фрагмент будет уничтожен,
            // данное меню было скрыто.
            }, viewLifecycleOwner)
            imageFromGallery.setOnClickListener {
                ImagePicker.with(this@NewPostFragment)
                    .galleryOnly()
                    .crop()
                    .compress(2048)
                    .createIntent {
                        photoLauncher.launch(it)
                    }
            }
            imageFromCamera.setOnClickListener {
                ImagePicker.with(this@NewPostFragment)
                    .cameraOnly()
                    .crop()
                    .compress(2048)
                    .createIntent(photoLauncher::launch)
            }
            clearPreview.setOnClickListener {
                viewModel.clearPhoto()
            }
            cancelEdit.setOnClickListener {
//                    viewModel.saveDraftCopy(null)
                customNavigateUp()
            }
        }
    }

    private fun subscribe() {
        viewModel.apply {
            media.observe(viewLifecycleOwner) { image ->
                binding.previewContainer.isVisible = (image != null)
                binding.imagePreview.setImageURI(image?.uri)
            }
            postEvent.observe(viewLifecycleOwner) { code ->
                binding.apply {
                    if (code == HTTP_OK)
                        showToastAfterSave(
                            context,
                            root.context,
                            edited.value?.id,
                            arguments?.POST_CONTENT,
                            newContent.text.toString()
                        )
                    else
                        Toast.makeText(
                            context,
                            root.context.getString(
                                if (edited.value?.idFromServer == 0L)
                                    R.string.error_saving
                                else
                                    R.string.error_editing,
                                overview(code)
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                }
                    // Очистка черновика
//                saveDraftCopy(null)
                customNavigateUp()
            }
        }
    }

    private fun customNavigateUp() {
        viewModel.apply {
            clearEditedValue()
            clearPhoto()
            loadPosts()
        }
        findNavController().navigateUp()
    }
}