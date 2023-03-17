package ru.netology.nmedia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.AppActivity
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.dto.PushMessage
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()
    private val appAuth = DependencyContainer.getInstance().appAuth

    override fun onCreate() {
        super.onCreate()
        // Создание канала
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onNewToken(token: String) {
        Log.d("TOKEN: ", token)
        appAuth.sendPushToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val currentUserId = appAuth.data.value?.id
        val incomingContent = gson.fromJson(message.data[content], PushMessage::class.java)
        val incomingRecipientId = incomingContent.recipientId
        handleMessage(currentUserId, incomingRecipientId)
    }

    private fun handleMessage(currentUserId: Long?, recipientId: Long?) {
        if (recipientId != null && recipientId != currentUserId)
            appAuth.sendPushToken()
        else {
            val intent = Intent(applicationContext, AppActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                                    this,
                                    0,
                                    intent,
                                    PendingIntent.FLAG_IMMUTABLE
                                )
            val notification = NotificationCompat.Builder(this, channelId).apply {
                setSmallIcon(R.drawable.ic_notification)
                when (recipientId) {
                    null -> setContentTitle(getString(R.string.ads_push))
                    currentUserId -> {
                        val content = getString(R.string.ok_auth_push)
                        setContentText(content.lines().first())
                        setStyle( NotificationCompat.BigTextStyle().bigText(content) )
                    }
                }
                priority = NotificationCompat.PRIORITY_DEFAULT
                setContentIntent(pendingIntent)
                setAutoCancel(true)
            }.build()

            NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        }
    }

    fun onMessageReceivedOld(message: RemoteMessage) {
        // Реализация к заданию 4.3 Notifications & Pushes
        val incomingAction = message.data[action]
        val incomingContent = message.data[content]
        // Проверка входящего action'а на соответствие существующим в приложении
        if (Action.values().find { it.name == incomingAction } != null)
            handleMessageOld(incomingAction!!, gson.fromJson(incomingContent, MessageContent::class.java))
    }

    private fun handleMessageOld(action: String, content: MessageContent) {
        val intent = Intent(applicationContext, AppActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId).apply {
            setSmallIcon(R.drawable.ic_notification)
            when (Action.valueOf(action)) {
                Action.LIKE ->
                    setContentTitle( getString(R.string.notification_like, content.userName, content.postId, content.postAuthor) )
                Action.NEW_POST -> {
                    setContentTitle( getString(R.string.notification_new_post, content.userName) )
                    setContentText(content.postContent.lines().first())
                    setStyle( NotificationCompat.BigTextStyle().bigText(content.postContent) )
                }
            }
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }.build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }
}