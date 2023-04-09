package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.paging.*
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import okhttp3.internal.http.HTTP_BAD_REQUEST
import okhttp3.internal.http.HTTP_NOT_FOUND
import okhttp3.internal.http.HTTP_OK
import ru.netology.nmedia.R
import ru.netology.nmedia.util.CompanionNotMedia.ATTACHMENT_PREVIEW
import ru.netology.nmedia.util.CompanionNotMedia.POST_CONTENT
import ru.netology.nmedia.util.CompanionNotMedia.POST_ID
import ru.netology.nmedia.adapter.OnInteractionListenerImpl
import ru.netology.nmedia.adapter.PathPointerImpl
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.adapter.PostLoadingStateAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.model.RemotePresentationState.*
import ru.netology.nmedia.model.asRemotePresentationState
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment(R.layout.fragment_feed) {
    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var postAdapter: PostAdapter
    private lateinit var navController: NavController
    private var snackbar: Snackbar? = null
    private val Fragment.viewScope
        get() = viewLifecycleOwner.lifecycleScope

    override fun onDestroyView() {
        super.onDestroyView()
        snackbarDismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentFeedBinding.bind(view)
        initViews(binding)
        subscribe(binding)
        setupListeners(binding)
    }

    private fun initViews(binding: FragmentFeedBinding) {
        postAdapter = PostAdapter(
            OnInteractionListenerImpl(viewModel, authViewModel),
            PathPointerImpl(viewModel)
        )
        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        val loadStateHeader = PostLoadingStateAdapter { postAdapter.retry() }
        val loadStateFooter = PostLoadingStateAdapter { postAdapter.retry() }
        binding.recyclerView.posts.apply {
            addItemDecoration(decoration)
            adapter =
                postAdapter.withLoadStateHeaderAndFooter(
                    header = loadStateHeader,
                    footer = loadStateFooter
                )
        }
        navController = findNavController()
    }

    private fun subscribe(binding: FragmentFeedBinding) {
        viewModel.apply {
//            dataState.observe(viewLifecycleOwner) { state ->
//                binding.apply {
//                    progressBarView.progressBar.isVisible = state.loading
//                    errorView.errorTitle.isVisible = state.error
//                    recyclerViewAndEmptyView.isVisible = state.showing
//                    recyclerView.refreshPosts.isRefreshing = state.refreshing
//                }
//            }
            // Чтобы подписаться на PagingData<Post>, необходимо использовать
            // корутину Fragment'а
            viewScope.launchWhenCreated {
                dataFlow.collectLatest {
                    postAdapter.submitData(it)
                }
            }
            // А вот для отображения обновленного PostAdapter'а надо также
            // как и выше запустить корутину
            viewScope.launch {
                // В соответствии с рекомендациями, указанными в описании
                // метода lifecycleScope.launchWhenCreated{}, использован
                // метод lifecycle.repeatOnLifecycle().
                // Применение данного метода позволяет исключить NullPointerException
                // при обращении к свойству binding в adapter.loadStateFlow.collectLatest.
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    snackbarDismiss()
                    // Объект loadStateFlow отвечает за отображение загруженных
                    // данных в адаптер
                    val presented =
                        postAdapter.loadStateFlow
                            .asRemotePresentationState()
                            .map { state -> state == PRESENTED }
                    postAdapter.loadStateFlow.collectLatest {
                        // Индикатор обновления будет отображаться только когда
                        // происходит refresh
                        binding.apply {
//                            errorView.errorTitle.isVisible =
//                                it.refresh is LoadState.Error ||
//                                it.prepend is LoadState.Error ||
//                                it.append is LoadState.Error
//                            recyclerViewAndEmptyView.isVisible =
//                                it.refresh !is LoadState.Error &&
//                                it.prepend !is LoadState.Error &&
//                                it.append !is LoadState.Error
                            recyclerView.refreshPosts.isRefreshing =
                                it.refresh is LoadState.Loading
                        }
//                        when {
//                            it.refresh is LoadState.Error -> it.refresh as LoadState.Error
//                            it.prepend is LoadState.Error -> it.prepend as LoadState.Error
//                            it.append is LoadState.Error -> it.append as LoadState.Error
//                            else -> null
//                        }?.also { state ->
//                            state.error.message?.let { message ->
//                                snackbar = Snackbar.make(
//                                    binding.root,
//                                    message,
//                                    Snackbar.LENGTH_INDEFINITE
//                                )
//                                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
//                                    .setAction(R.string.retry_loading) {
//                                        snackbarDismiss()
//                                        postAdapter.refresh()
//                                    }
//                                snackbar?.show()
//                            }
//                        }
                    }
                }
            }
//            postLoadingStateAdapter.registerAdapterDataObserver(
//                object : AdapterDataObserver() {
//                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                        binding.recyclerView.posts.scrollToPosition(0)
//                    }
//                }
//            )
            // Добавление плавного скролла при добавлении новых постов
//            postAdapter.registerAdapterDataObserver(
//                object : AdapterDataObserver() {
//                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                        viewScope.launch {
//                            // Если что-то добавилось наверх списка,
//                            postAdapter.loadStateFlow.collectLatest {
//                                if ((it.refresh is LoadState.Loading ||
//                                     it.prepend is LoadState.Loading) &&
//                                        positionStart == 0)
//                                    // тогда плавно заскроллиться до самого верха
//                                    binding.recyclerView.posts.smoothScrollToPosition(0)
//                            }
//                        }
//                    }
//                }
//            )
//            newerCount.observe(viewLifecycleOwner) { count ->
//                binding.recyclerView.newPosts.apply {
//                    isVisible = (count != null && count != 0)
//                }
//            }
//            postEvent.observe(viewLifecycleOwner) { code ->
//                if (code != HTTP_OK) {
//                    snackbar = Snackbar.make(
//                        binding.root,
//                        overview(code),
//                        Snackbar.LENGTH_INDEFINITE
//                    )
//                        .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
//                        .setAction(R.string.retry_loading) {
//                            adapter.refresh()
//                            flowPosts()
//                        }
//                    snackbar?.show()
//                }
//            }
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
                snackbarDismiss()
                postAdapter.refresh()
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
                    postAdapter.refresh()
                }
            }
        }
    }

    private fun setupListeners(binding: FragmentFeedBinding) {
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
                postAdapter.refresh()
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

    private fun snackbarDismiss() {
        if (snackbar != null && snackbar?.isShown == true) {
            snackbar?.dismiss()
            snackbar = null
        }
    }
}