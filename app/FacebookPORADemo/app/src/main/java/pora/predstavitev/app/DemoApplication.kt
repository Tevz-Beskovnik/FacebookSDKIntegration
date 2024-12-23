package pora.predstavitev.app

import android.app.Application
import android.util.Log
import com.facebook.AccessToken
import com.facebook.CallbackManager

val PUBLIC_PROFILE = "public_profile"
val EMAIL = "email"

class DemoApplication : Application() {
    lateinit var userName: String
    lateinit var userEmail: String
    lateinit var callbackManager: CallbackManager
    var accessToken: AccessToken? = null

    override fun onCreate() {
        super.onCreate()
        callbackManager = CallbackManager.Factory.create()
        userName = ""
        userEmail = ""

        Log.i("ApplicationCreate", "Set value for userName and userEmail")
    }
}