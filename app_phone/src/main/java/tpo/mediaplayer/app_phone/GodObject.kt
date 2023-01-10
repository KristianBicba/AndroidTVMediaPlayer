package tpo.mediaplayer.app_phone

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy

class GodObject : Application() {
    private fun allowNetworkOnMain() {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    override fun onCreate() {
        super.onCreate()
        allowNetworkOnMain()
    }
}