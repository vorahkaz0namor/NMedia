package ru.netology.nmedia.activity

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.paging.*
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import okhttp3.internal.http.HTTP_BAD_REQUEST
import okhttp3.internal.http.HTTP_NOT_FOUND
import okhttp3.internal.http.HTTP_OK
import ru.netology.nmedia.R
import ru.netology.nmedia.util.CompanionNotMedia.ATTACHMENT_PREVIEW
import ru.netology.nmedia.util.CompanionNotMedia.POST_CONTENT
import ru.netology.nmedia.util.CompanionNotMedia.POST_ID
import ru.netology.nmedia.adapter.OnInteractionListenerImpl
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.util.CompanionNotMedia.overview
import ru.netology.nmedia.util.viewBinding
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment(R.layout.fragment_feed) {
    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private val binding by viewBinding(FragmentFeedBinding::bind)
    private lateinit var adapter: PostAdapter
    private lateinit var navController: NavController
    private var snackbar: Snackbar? = null
    private lateinit var killingJob: Job
    private lateinit var killingObserver: AdapterDataObserver

    override fun onDestroyView() {
        super.onDestroyView()
        // Костыли после реализации бибилиотеки Paging,
        // но они с каждым разом увеличивают количество запросов
        killingJob.cancel()
        adapter.unregisterAdapterDataObserver(killingObserver)
        Log.d("JOB IS DEAD", "${killingJob.isActive}")
        Log.d("OBS. IS DEAD", "${killingObserver.javaClass}")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribe()
        setupListeners()
    }

    private fun initViews() {
        adapter = PostAdapter(OnInteractionListenerImpl(viewModel, authViewModel))
        binding.recyclerView.posts.adapter = adapter
        navController = findNavController()
    }

    private fun subscribe() {
        viewModel.apply {
            dataState.observe(viewLifecycleOwner) { state ->
                binding.apply {
                    progressBarView.progressBar.isVisible = state.loading
                    errorView.errorTitle.isVisible = state.error
                    recyclerViewAndEmptyView.isVisible = state.showing
                    recyclerView.refreshPosts.isRefreshing = state.refreshing
                }
            }
            // Чтобы подписаться на PagingData<Post>, необходимо использовать
            // корутину Fragment'а
            lifecycleScope.launchWhenCreated {
                data.collectLatest {
                    adapter.submitData(it)
                }
            }
            // А вот для отображения обноленного PostAdapter'а надо также
            // как и выше запустить корутину
            lifecycleScope.launchWhenCreated {
                // Объект loadStateFlow отвечает за отображение загруженных
                // данных в адаптер
                adapter.loadStateFlow.collectLatest {
                    // Индикатор обновления будет отображаться, когда
                    // происходит refresh, либо когда запрашивается следующая
                    // страница, либо когда запрашивается предыдущая страница
                    binding.recyclerView.refreshPosts.isRefreshing =
                        it.refresh is LoadState.Loading ||
                        it.append is LoadState.Loading ||
                        // При пролистывании вверх (запросе предыдущей страницы)
                        // ничего не будет происходить, поскольку в классе PostPagingSource
                        // состояние LoadParams.Prepend не обрабатывается
                        it.prepend is LoadState.Loading
                }

            }.also {
                killingJob = it
                Log.d("JOB IS CREATED", "${killingJob.isActive}")
            }
            // Добавление плавного скролла при добавлении новых постов
            adapter.registerAdapterDataObserver(
                object : AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        // Если что-то добавилось наверх списка,
                        if (positionStart == 0)
                            // тогда плавно заскроллиться до самого верха
                            binding.recyclerView.posts.smoothScrollToPosition(0)
                    }
                }
                    .also {
                        killingObserver = it
                        Log.d("OBS. IS CREATED", "${killingObserver.javaClass}")
                    }
            )
//            newerCount.observe(viewLifecycleOwner) { count ->
//                binding.recyclerView.newPosts.apply {
//                    isVisible = (count != null && count != 0)
//                }
//            }
            postEvent.observe(viewLifecycleOwner) { code ->
                if (code != HTTP_OK) {
                    snackbar = Snackbar.make(
                        binding.root,
                        overview(code),
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                        .setAction(R.string.retry_loading) {
                            loadPosts()
                        }
                    snackbar?.show()
                }
            }
            edited.observe(viewLifecycleOwner) { post ->
                if (post.id != 0L)
                    navController.navigate(
                        R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply { POST_CONTENT = post.content }
                    )
            }
            hasShared.observe(viewLifecycleOwner) { post ->
                if (post.id != 0L)
                    navController.navigate(
                        R.id.action_feedFragment_to_sharePostFragment,
                        Bundle().apply { POST_CONTENT = post.content }
                    )
            }
            singlePostToView.observe(viewLifecycleOwner) { post ->
                if (post.id != 0L) {
                    navController.navigate(
                        R.id.action_feedFragment_to_singlePostFragment,
                        Bundle().apply {
                            POST_ID = post.id
                            ATTACHMENT_PREVIEW = "Post view"
                        }
                    )
                }
            }
            viewingAttachments.observe(viewLifecycleOwner) { post ->
                if (post.id != 0L) {
                    navController.navigate(
                        R.id.action_feedFragment_to_attachmentsFragment
                    )
                }
            }
        }
        authViewModel.apply {
            data.observe(viewLifecycleOwner) {
                if (snackbar != null && snackbar?.isShown == true)
                    snackbar?.dismiss()
                viewModel.refresh()
            }
            checkAuthorized.observe(viewLifecycleOwner) {
                if (it) {
                    if (!authViewModel.authorized)
                        AuthDialogFragment().show(
                            childFragmentManager,
                            AuthDialogFragment.AUTH_TAG
                        )
                }
            }
            authError.observe(viewLifecycleOwner) { code ->
                if ( code != HTTP_OK &&
                    (code != HTTP_BAD_REQUEST || code != HTTP_NOT_FOUND) ) {
                    clearAuthError()
                    viewModel.refresh()
                }
            }
        }

    }

    private fun setupListeners() {
        binding.recyclerView.apply {
            addNewPost.setOnClickListener {
                if (!authViewModel.authorized)
                    AuthDialogFragment().show(
                        childFragmentManager,
                        AuthDialogFragment.AUTH_TAG
                    )
                else {
                    viewModel.getDraftCopy()
                    navController.navigate(
                        R.id.action_feedFragment_to_newPostFragment
                    )
                }
            }
            refreshPosts.setOnRefreshListener {
                // Для реализации обновления данных необходимо
                // вместо обновления списка постов через ViewModel
                // запустить обновление PostAdapter'а
                adapter.refresh()
            }
            toLoadSampleImage.setOnClickListener {
                navController.navigate(R.id.action_feedFragment_to_sampleFragment)
            }
            newPosts.setOnClickListener {
                it.isVisible = false
                viewModel.showUnreadPosts()
            }
        }
    }
}