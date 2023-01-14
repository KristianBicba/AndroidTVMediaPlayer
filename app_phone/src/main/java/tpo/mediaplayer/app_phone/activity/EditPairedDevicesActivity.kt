package tpo.mediaplayer.app_phone.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import tpo.mediaplayer.app_phone.GodObject
import tpo.mediaplayer.app_phone.R
import tpo.mediaplayer.app_phone.attemptPairing
import tpo.mediaplayer.app_phone.db.Device
import tpo.mediaplayer.app_phone.pairingDataFromHexString
import tpo.mediaplayer.app_phone.recyclerViewAdapter.PairedDeviceAdapter

class EditPairedDevicesActivity : AppCompatActivity() {
    private lateinit var vDeviceList: RecyclerView
    private lateinit var vNoDevicesImage: ImageView
    private lateinit var vNoDevicesText: TextView
    private lateinit var vAddDevice: Button

    private lateinit var scannerLauncher: ActivityResultLauncher<ScanOptions>

    private fun registerScannerLauncher() {
        scannerLauncher = registerForActivityResult(ScanContract()) {
            val codeParsed = kotlin.run {
                val contents = it.contents ?: return@run false
                val pairingData = pairingDataFromHexString(contents) ?: return@run false
                attemptPairing(pairingData, GodObject.instance.guid, this)
                true
            }
            if (!codeParsed)
                Toast.makeText(
                    applicationContext,
                    getString(R.string.pairing_status_scanfailure),
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    private fun scanDevice() {
        val options = ScanOptions().apply {
            setPrompt(getString(R.string.pairing_prompt))
            setBeepEnabled(false)
            setOrientationLocked(true)
            captureActivity = ScanQRCodeActivity::class.java
        }

        scannerLauncher.launch(options)
    }

    private fun onClickDevice(device: Device) {
        AlertDialog.Builder(this).apply {
            setMessage(getString(R.string.edit_paired_devices_delete_prompt) + " ${device.name}?")
            setNegativeButton(getString(R.string.no), null)
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                GodObject.instance.db.deviceDao().deleteDeviceByUid(device.uid)
            }
            show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_paired_devices)

        supportActionBar?.hide()

        vDeviceList = findViewById(R.id.recyclerView)
        vNoDevicesImage = findViewById(R.id.empty_image_view)
        vNoDevicesText = findViewById(R.id.empty_data_text)
        vAddDevice = findViewById(R.id.add_device)

        vAddDevice.setOnClickListener { scanDevice() }

        val adapter = PairedDeviceAdapter(::onClickDevice)
        vDeviceList.adapter = adapter

        GodObject.instance.db.deviceDao().getAllDevicesLive().observe(this) {
            if (it == null || it.isEmpty()) {
                vNoDevicesImage.visibility = View.VISIBLE
                vNoDevicesText.visibility = View.VISIBLE
            } else {
                vNoDevicesImage.visibility = View.GONE
                vNoDevicesText.visibility = View.GONE
            }
            adapter.submitList(it)
        }

        registerScannerLauncher()
    }
}