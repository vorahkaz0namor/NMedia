package ru.netology.nmedia.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.NavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import ru.netology.nmedia.R
import ru.netology.nmedia.util.CompanionNotMedia.POST_CONTENT

class AppActivity : AppCompatActivity(R.layout.activity_app) {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        checkoutGoogleApiAvailability()
    }

    private fun init() {
        intent?.let {
            if (it.action == Intent.ACTION_SEND) {
                val text = it.getStringExtra(Intent.EXTRA_TEXT)
                if (!text.isNullOrBlank()) {
                    intent.removeExtra(Intent.EXTRA_TEXT)
                    navController.navigate(
                        R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply { POST_CONTENT = text }
                    )
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