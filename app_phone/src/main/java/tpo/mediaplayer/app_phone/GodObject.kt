package tpo.mediaplayer.app_phone

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import androidx.room.Room
import org.bouncycastle.jce.provider.BouncyCastleProvider
import tpo.mediaplayer.app_phone.db.AppDatabase
import java.security.Security

class GodObject : Application() {
    lateinit var db: AppDatabase
        private set

    val deviceName by lazy { "${Build.MANUFACTURER} ${Build.MODEL}" }

    private fun setupBouncyCastle() {
        val provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)
            ?: // Web3j will set up the provider lazily when it's first used.
            return
        if (provider::class.java.equals(BouncyCastleProvider::class.java)) {
            // BC with same package name, shouldn't happen in real life.
            return
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }

    private fun setupDatabase() {
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "device-database"
        ).allowMainThreadQueries().build()
    }

    private fun allowNetworkOnMain() {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        setupBouncyCastle()
        setupDatabase()
        allowNetworkOnMain()
    }

    companion object {
        @JvmStatic
        lateinit var instance: GodObject
            private set
    }
}