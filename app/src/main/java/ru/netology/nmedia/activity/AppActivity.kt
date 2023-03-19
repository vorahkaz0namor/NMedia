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
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.util.CompanionNotMedia.POST_CONTENT
import ru.netology.nmedia.viewmodel.AuthViewModel

@AndroidEntryPoint
class AppActivity : AppCompatActivity(R.layout.activity_app) {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var nmediaNavController: NavController
    private val authViewModel: AuthViewModel by viewModels()
    private var currentMenuProvider: MenuProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setAuthMenu()
        subscribe()
        setupListeners()
        checkoutGoogleApiAvailability()
    }

    private fun init() {
        nmediaNavController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(nmediaNavController.graph)
        setupActionBarWithNavController(nmediaNavController, appBarConfiguration)
        intent?.let {
            if (it.action == Intent.ACTION_SEND) {
                val text = it.getStringExtra(Intent.EXTRA_TEXT)
                if (!text.isNullOrBlank()) {
                    intent.removeExtra(Intent.EXTRA_TEXT)
                    nmediaNavController.navigate(
                        R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply { POST_CONTENT = text }
                    )
                }
            }
        }
    }

    private fun setAuthMenu() {
            if (currentMenuProvider == null) {
                currentMenuProvider = object : MenuProvider {
                    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                        menuInflater.inflate(R.menu.menu_auth, menu)
                        menu.setGroupVisible(R.id.unauthorized, !authViewModel.authorized)
                        menu.setGroupVisible(R.id.authorized, authViewModel.authorized)
                    }

                    override fun onMenuItemSelected(menuItem: MenuItem) =
                        when (menuItem.itemId) {
                            R.id.login -> {
                                authViewModel.authShowing()
                                nmediaNavController.navigate(R.id.loginFragment)
                                true
                            }
                            R.id.register -> {
                                authViewModel.regShowing()
                                nmediaNavController.navigate(R.id.loginFragment)
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
                }
                addMenuProvider(currentMenuProvider as MenuProvider, this)
            }
    }

    private fun subscribe() {
        authViewModel.data.observe(this) {
            setAuthMenu()
        }
    }

    private fun setupListeners() {
        nmediaNavController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.feedFragment -> if (currentMenuProvider == null) setAuthMenu()
                R.id.loginFragment -> currentMenuProvider?.let {
                    removeMenuProvider(it)
                    currentMenuProvider = null
                }
            }
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