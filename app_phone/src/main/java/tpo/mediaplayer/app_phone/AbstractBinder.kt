package tpo.mediaplayer.app_phone

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

abstract class AbstractBinder<B : IBinder>(
    private val context: Context,
    private val serviceClass: Class<*>
) {
    var binder: B? = null
        private set

    private var shouldUnbind = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            @Suppress("UNCHECKED_CAST")
            binder = service as B
            onBind(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
            onUnbind(null)
        }
    }

    fun bind(): Boolean {
        val intent = Intent(context, serviceClass)
        shouldUnbind = true
        return context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbind() {
        if (shouldUnbind) {
            shouldUnbind = false
            onUnbind(binder)
            context.unbindService(connection)
        }
    }

    protected abstract fun onBind(binder: B)
    protected abstract fun onUnbind(binder: B?)
}