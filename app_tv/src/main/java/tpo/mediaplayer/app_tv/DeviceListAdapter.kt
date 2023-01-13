package tpo.mediaplayer.app_tv

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tpo.mediaplayer.app_tv.db.Device

private object DeviceDiffCallback : DiffUtil.ItemCallback<Device>() {
    override fun areItemsTheSame(oldItem: Device, newItem: Device) = oldItem.uid == newItem.uid

    override fun areContentsTheSame(oldItem: Device, newItem: Device) =
        oldItem.uid == newItem.uid
                && oldItem.deviceName == newItem.deviceName
                && oldItem.communicationStr == newItem.communicationStr
}

class NewDeviceListAdapter(private val onClick: (Int) -> Unit) :
    ListAdapter<Device, NewDeviceListAdapter.ViewHolder>(DeviceDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)!!
        holder.textView.text = item.deviceName
        holder.relativeLayout.setOnClickListener { onClick(item.uid) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val relativeLayout: RelativeLayout = itemView.findViewById(R.id.relativeLayout)

        init {
            itemView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                if (hasFocus) textView.setBackgroundColor(Color.parseColor("#FFBB86FC"))
                else textView.setBackgroundColor(Color.parseColor("#FF3700B3"))
            }
        }
    }
}