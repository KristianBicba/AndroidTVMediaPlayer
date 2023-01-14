package tpo.mediaplayer.app_phone.recyclerViewAdapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tpo.mediaplayer.app_phone.R
import tpo.mediaplayer.lib_vfs.VfsDirEntry

private object VfsDirEntryDiffCallback : DiffUtil.ItemCallback<VfsDirEntry>() {
    override fun areItemsTheSame(oldItem: VfsDirEntry, newItem: VfsDirEntry) = oldItem.path == newItem.path
    override fun areContentsTheSame(oldItem: VfsDirEntry, newItem: VfsDirEntry) = oldItem.path == newItem.path
}

class FileSystemAdapter(private val onClick: (VfsDirEntry) -> Unit) :
    ListAdapter<VfsDirEntry, FileSystemAdapter.ViewHolder>(VfsDirEntryDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_row_file, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)!!
        holder.name.text = "${item.name}${if (item.isDirectory) "/" else ""}"
        holder.container.setOnClickListener { onClick(item) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.row_file_text_name)
        val container: LinearLayout = view.findViewById(R.id.row_file_container)
    }
}