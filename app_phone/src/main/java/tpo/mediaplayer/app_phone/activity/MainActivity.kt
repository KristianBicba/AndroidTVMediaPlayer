package tpo.mediaplayer.app_phone.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import tpo.mediaplayer.app_phone.GodObject
import tpo.mediaplayer.app_phone.R
import tpo.mediaplayer.app_phone.db.Device

class MainActivity : AppCompatActivity() {
    private lateinit var vEditDevices: Button
    private lateinit var vEditMediaServers: Button
    private lateinit var vConnect: Button

    private lateinit var availableDevices: List<Device>

    private fun loadAvailableDevices() {
        availableDevices = GodObject.instance.db.deviceDao().getAllDevices()
        vConnect.isEnabled = !availableDevices.isEmpty()
    }

    private fun requestRemoteControl(device: Device) {
        val intent = Intent(this, RemoteControlActivity::class.java)
        intent.putExtra("server_uid", device.uid)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        supportActionBar?.hide()

        vEditDevices = findViewById(R.id.main_button_paired_devices)
        vEditMediaServers = findViewById(R.id.main_button_mediaservers)
        vConnect = findViewById(R.id.main_button_connect)

        vEditDevices.setOnClickListener {
            val intent = Intent(this, EditPairedDevicesActivity::class.java)
            startActivity(intent)
        }

        vEditMediaServers.setOnClickListener {
            val intent = Intent(this, EditMediaServersActivity::class.java)
            startActivity(intent)
        }

        vConnect.setOnClickListener {
            loadAvailableDevices()
            if (availableDevices.isEmpty()) return@setOnClickListener

            val mapping = mutableMapOf<MenuItem, Device>()
            val popup = PopupMenu(this, vConnect)
            for (device in availableDevices) {
                val item = popup.menu.add(device.name)
                mapping += item to device
            }

            popup.setOnMenuItemClickListener {
                requestRemoteControl(mapping[it] ?: return@setOnMenuItemClickListener true)
                true
            }

            popup.show()
        }
    }

    override fun onStart() {
        super.onStart()
        loadAvailableDevices()
    }
}