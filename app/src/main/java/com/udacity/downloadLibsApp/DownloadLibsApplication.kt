package com.udacity.downloadLibsApp

import android.app.Application
import timber.log.Timber

class DownloadLibsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
