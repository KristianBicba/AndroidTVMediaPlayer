package tpo.mediaplayer.app_phone.recyclerViewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tpo.mediaplayer.app_phone.R
import tpo.mediaplayer.app_phone.db.Device
import tpo.mediaplayer.app_phone.hexDecode
import java.io.IOException
import java.net.InetAddress

private object DeviceDiffCallback : DiffUtil.ItemCallback<Device>() {
    override fun areItemsTheSame(oldItem: Device, newItem: Device) = oldItem == newItem
    override fun areContentsTheSame(oldItem: Device, newItem: Device) = oldItem == newItem
}

class PairedDeviceAdapter(private val onClick: (Device) -> Unit) :
    ListAdapter<Device, PairedDeviceAdapter.ViewHolder>(DeviceDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_row_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = getItem(position)!!
        holder.deviceId.text = device.uid.toString()
        holder.deviceName.text = device.name
        holder.deviceInfo.text = kotlin.run {
            val hexEncodedInetAddress = device.address
            val bytestreamInetAddress = hexDecode(hexEncodedInetAddress) ?: return@run null
            val actualInetAddress = try {
                InetAddress.getByAddress(bytestreamInetAddress)
            } catch (_: IOException) {
                return@run null
            }
            actualInetAddress.toString()
        } ?: "ERROR"
        holder.container.setOnClickListener { onClick(device) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceId: TextView = view.findViewById(R.id.row_device_text_id)
        val deviceName: TextView = view.findViewById(R.id.row_device_text_name)
        val deviceInfo: TextView = view.findViewById(R.id.row_device_text_info)
        val container: LinearLayout = view.findViewById(R.id.row_device_container)
    }
}