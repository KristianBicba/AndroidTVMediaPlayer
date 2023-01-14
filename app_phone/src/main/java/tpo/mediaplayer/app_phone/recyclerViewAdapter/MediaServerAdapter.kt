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
import tpo.mediaplayer.app_phone.db.MediaServer

private object MediaServerDiffCallback : DiffUtil.ItemCallback<MediaServer>() {
    override fun areItemsTheSame(oldItem: MediaServer, newItem: MediaServer) = oldItem == newItem
    override fun areContentsTheSame(oldItem: MediaServer, newItem: MediaServer) = oldItem == newItem
}

class MediaServerAdapter(private val onClick: (MediaServer) -> Unit) :
    ListAdapter<MediaServer, MediaServerAdapter.ViewHolder>(MediaServerDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_row_mediaserver, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaServer = getItem(position)!!
        holder.id.text = mediaServer.uid.toString()
        holder.name.text = mediaServer.name
        holder.path.text = mediaServer.connectionString
        holder.container.setOnClickListener { onClick(mediaServer) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val id: TextView = view.findViewById(R.id.row_mediaserver_text_id)
        val name: TextView = view.findViewById(R.id.row_mediaserver_text_name)
        val path: TextView = view.findViewById(R.id.row_mediaserver_text_info)
        val container: LinearLayout = view.findViewById(R.id.row_mediaserver_container)
    }
}