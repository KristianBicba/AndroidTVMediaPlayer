package tpo.mediaplayer.app_tv.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import tpo.mediaplayer.app_tv.*
import tpo.mediaplayer.app_tv.db.Device
import tpo.mediaplayer.app_tv.db.DeviceDao
import tpo.mediaplayer.app_tv.service.MainServerService
import tpo.mediaplayer.app_tv.service.VideoPlayerLauncherService
import kotlin.math.min

class PairingActivity : AppCompatActivity() {
    private val server by lazy {
        object : AbstractBinder<MainServerService.LocalBinder>(this, MainServerService::class.java) {
            override fun onBind(binder: MainServerService.LocalBinder) {
                val pairingData = binder.setPairing(true) ?: return
                createQRCode(hexEncode(pairingData))
            }

            override fun onUnbind(binder: MainServerService.LocalBinder?) {
                binder?.setPairing(false)
            }
        }
    }

    private val db = GodObject.instance.db
    private val handler by lazy { Handler(mainLooper) }

    private lateinit var vQRCode: ImageView
    private lateinit var vDeviceName: TextView
    private lateinit var vDeviceList: RecyclerView

    private fun setupRecyclerView(clickCallback: (Int) -> Unit) {
        val adapter = NewDeviceListAdapter(clickCallback)
        var first = true
        db.deviceDao().allLive.observe(this) {
            adapter.submitList(it)
            if (first) {
                first = false
                handler.post { vDeviceList.requestFocus() }
            }
        }
        vDeviceList.setHasFixedSize(true)
        vDeviceList.adapter = adapter
    }

    private fun createQRCode(connectionString: String) {
        val width = vQRCode.width
        val height = vQRCode.height
        val qrgEncoder = QRGEncoder(connectionString, null, QRGContents.Type.TEXT, min(width, height))
        val bitmap = qrgEncoder.encodeAsBitmap()
        vQRCode.setImageBitmap(bitmap)
    }

    private fun clickDevice(uid: Int) {
        val device = db.deviceDao().getByUid(uid)
        AlertDialog.Builder(this).run {
            setMessage("Really delete client \"${device.deviceName}\"?")
            setNegativeButton("No", null)
            setPositiveButton("Yes") { _, _ ->
                db.deviceDao().delete(device)
                server.binder?.disconnectClient(device.communicationStr)
            }
            create()
        }.apply {
            setOnShowListener {
                val button = getButton(AlertDialog.BUTTON_NEGATIVE)
                button.requestFocus()
            }
            show()
        }
    }

    @Suppress("unused")
    private fun testFillDevicesDb(deviceDao: DeviceDao) {
        if (deviceDao.all.size == 0) {
            for (i in 0..9) {
                val device = Device()
                device.deviceName = "test$i"
                device.communicationStr = "1232456765432-$i"
                deviceDao.insert(device)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_code)
//        testFillDevicesDb(db.deviceDao())

        vQRCode = findViewById(R.id.qrCode)
        vDeviceName = findViewById(R.id.deviceName)
        vDeviceList = findViewById(R.id.deviceList)

        vDeviceName.text = GodObject.instance.deviceName
        setupRecyclerView(::clickDevice)
    }

    override fun onStart() {
        super.onStart()
        startService(Intent(this, VideoPlayerLauncherService::class.java))
        server.bind()
    }

    override fun onStop() {
        super.onStop()
        server.unbind()
    }
}