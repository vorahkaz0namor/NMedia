package ru.netology.nmedia.activity

import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.flow.*
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
import ru.netology.nmedia.model.UiAction
import ru.netology.nmedia.model.asRemotePresentationState
import ru.netology.nmedia.util.CompanionNotMedia.allStatesToString
import ru.netology.nmedia.util.CompanionNotMedia.overview
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment(R.layout.fragment_feed) {
    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var postAdapter: PostAdapter
    private lateinit var loadStateHeader: PostLoadingStateAdapter
    private lateinit var loadStateFooter: PostLoadingStateAdapter
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
        loadStateHeader = PostLoadingStateAdapter { postAdapter.retry() }
        loadStateFooter = PostLoadingStateAdapter { postAdapter.retry() }
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

    @OptIn(ExperimentalCoroutinesApi::class)
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
                    snackbarDismiss()
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
                    // Объект loadStateFlow отвечает за отображение загруженных
                    // данных в адаптер
                    val presented = postAdapter.loadStateFlow
                        .asRemotePresentationState()
                            .mapLatest { state ->
                                snackbarDismiss()
                                Log.d("ADAPTER LOAD STATE", state.name)
                                state == PRESENTED
                            }
                    val hasNotScrolledForCurrentId =
                        totalState // StateFlow<UiState>
                            .map {
                                it.hasNotScrolledForCurrentId
                            } // Flow<Boolean>
                            .distinctUntilChanged()
                    val shouldScrollToTop =
                        combine(presented, hasNotScrolledForCurrentId) { conditionOne, conditionTwo ->
                            conditionOne.and(conditionTwo)
                        }
                            .distinctUntilChanged()
                    // Скролл к верхнему элементу списка, если произошел не пустой
                    // refresh
                    shouldScrollToTop.collectLatest {
                        Log.d("SHOULD SCROLL TO TOP?", "$it")
                        if (it) {
                            binding.recyclerView.posts.smoothScrollToPosition(0)
                            val currentId = totalState.value.id
                            Log.d("WRITE STATE.SCROLL", "$currentId")
                            stateChanger(UiAction.Scroll(currentId = currentId))
                        }
                    }
                }
            }
            viewScope.launch {
                postAdapter.loadStateFlow.collectLatest { loadState ->
                    snackbarDismiss()
                    var headerStateName: String
                    var footerStateName: String
                    loadStateHeader.loadState =
                        loadState.mediator?.refresh
                            .takeIf {
                                it is LoadState.Loading || it is LoadState.Error
                            }.also { headerStateName = "mediator.refresh" }
                        ?: loadState.mediator?.prepend
                            .takeIf {
                                it is LoadState.Loading || it is LoadState.Error
                            }.also { headerStateName = "mediator.prepend" }
                        ?: loadState.source.refresh.also { headerStateName = "source.refresh" }
                    loadStateFooter.loadState =
                        loadState.mediator?.append
                            .takeIf {
                                it is LoadState.Loading || it is LoadState.Error
                            }.also { footerStateName = "mediator.append" }
                            ?: loadState.source.append.also { footerStateName = "source.append" }
//                    Log.d("HEADER & FOOTER",
//                        "INCOMING STATE =\n${loadState.allStatesToString()}\n" +
//                                "HEADER STATE =\n$headerStateName = ${loadStateHeader.loadState}\n" +
//                                "FOOTER STATE =\n$footerStateName = ${loadStateFooter.loadState}")
                    val errorState = loadState.refresh as? LoadState.Error
                        ?: loadState.prepend as? LoadState.Error
                        ?: loadState.append as? LoadState.Error
                    binding.apply {
                        // Индикатор обновления будет отображаться только когда
                        // происходит refresh
                        recyclerView.refreshPosts.isRefreshing =
                            loadState.mediator?.refresh is LoadState.Loading
                        recyclerViewAndEmptyView.isVisible = errorState !is LoadState.Error
                        if (errorState is LoadState.Error) {
                            errorView.errorTitle.isVisible = true
                            recyclerView.refreshPosts.isRefreshing = false
                        } else
                            errorView.errorTitle.isVisible = false
                    }
                    errorState?.let {
                                snackbar = Snackbar.make(
                                    binding.root,
                                    it.error.message ?: overview(520),
                                    Snackbar.LENGTH_INDEFINITE
                                )
                                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                                    .setAction(R.string.retry_loading) {
                                        snackbarDismiss()
                                        postAdapter.refresh()
                                    }
                                snackbar?.show()
                    }
                }
            }
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