package com.babedev.cheapmunk.app

import android.support.multidex.MultiDexApplication
import com.babedev.cheapmunk.domain.model.User
import com.crashlytics.android.Crashlytics
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.FirebaseApp
import io.fabric.sdk.android.Fabric

/**
 * @author BabeDev
 */
class MyApp : MultiDexApplication() {

    companion object {
        val me = User("")
    }

    override fun onCreate() {
        super.onCreate()

        Fabric.with(this, Crashlytics())
        FirebaseApp.initializeApp(this)
        FacebookSdk.sdkInitialize(this)
        AppEventsLogger.activateApp(this)
    }
}