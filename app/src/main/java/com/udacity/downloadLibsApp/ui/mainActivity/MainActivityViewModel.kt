package com.udacity.downloadLibsApp.ui.mainActivity

import android.app.Application
import android.app.NotificationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.udacity.downloadLibsApp.R
import com.udacity.downloadLibsApp.ui.util.cancelNotifications
import com.udacity.downloadLibsApp.ui.util.sendNotification

class MainActivityViewModel(private val app: Application) : AndroidViewModel(app) {
    private val notificationManager = ContextCompat.getSystemService(
        app,
        NotificationManager::class.java
    ) as NotificationManager

    fun sendDownloadNotification() {
        notificationManager.sendNotification(app.getString(R.string.notification_description), app)
    }

    fun cancelNotifications() {
        notificationManager.cancelNotifications()
    }

}
