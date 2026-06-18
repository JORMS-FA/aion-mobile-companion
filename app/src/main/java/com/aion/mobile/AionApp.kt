package com.aion.mobile

import android.app.Application
import com.aion.mobile.notification.NotificationHelper

class AionApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
