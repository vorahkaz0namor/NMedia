package ru.netology.nmedia.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.util.CompanionNotMedia.POST_CONTENT
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

class AppActivity : AppCompatActivity(R.layout.activity_app) {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setAuthMenu()
        checkoutGoogleApiAvailability()
    }

    private fun init() {
        intent?.let {
            if (it.action == Intent.ACTION_SEND) {
                val text = it.getStringExtra(Intent.EXTRA_TEXT)
                if (!text.isNullOrBlank()) {
                    intent.removeExtra(Intent.EXTRA_TEXT)
                    findNavController(R.id.nav_host_fragment).navigate(
                        R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply { POST_CONTENT = text }
                    )
                }
            }
        }
    }

    private fun setAuthMenu() {
        var previousMenuProvider: MenuProvider? = null

        authViewModel.data.observe(this) {
            // Чистим предыдущее меню
            previousMenuProvider?.let { removeMenuProvider(it) }
            // Создаем новое меню
            addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_auth, menu)
                    menu.setGroupVisible(R.id.unauthorized, !authViewModel.authorized)
                    menu.setGroupVisible(R.id.authorized, authViewModel.authorized)
                }
                override fun onMenuItemSelected(menuItem: MenuItem) =
                    when (menuItem.itemId) {
                        R.id.login -> {
                            findNavController(R.id.nav_host_fragment)
                                .navigate(R.id.loginFragment)
                            true
                        }
                        R.id.logout -> {
                            AuthDialogFragment().show(
                                supportFragmentManager,
                                AuthDialogFragment.AUTH_TAG
                            )
                            true
                        }
                        else -> false
                    }
                // Сохраняем новое меню как предыдущее
            }.also { previousMenuProvider = it })
        }
    }

    private fun checkoutGoogleApiAvailability() {
        GoogleApiAvailability.getInstance().apply {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            val requestCode = 9000
            if (code != ConnectionResult.SUCCESS)
                if (isUserResolvableError(code))
                    getErrorDialog(
                        this@AppActivity,
                        code,
                        requestCode
                    )?.show()
                else
                    Toast.makeText(
                        this@AppActivity,
                        R.string.no_google_api,
                        Toast.LENGTH_LONG
                    ).show()
        }
    }
}