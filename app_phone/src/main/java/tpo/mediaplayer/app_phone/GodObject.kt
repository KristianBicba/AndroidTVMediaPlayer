package tpo.mediaplayer.app_phone

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import java.net.InetAddress

class GodObject : Application() {
    class BrowsingSession(
        val clientAddress: InetAddress,
        val serverConnString: String,
        var currentPath: String
    )

    var session: BrowsingSession? = null

    private fun allowNetworkOnMain() {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        allowNetworkOnMain()
    }

    companion object {
        lateinit var INSTANCE: GodObject
    }
}