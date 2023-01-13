package tpo.mediaplayer.app_phone.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import tpo.mediaplayer.app_phone.GodObject
import tpo.mediaplayer.app_phone.R
import tpo.mediaplayer.app_phone.db.MediaServer
import tpo.mediaplayer.app_phone.recyclerViewAdapter.MediaServerAdapter

class EditMediaServersActivity : AppCompatActivity() {
    private lateinit var vMediaServerList: RecyclerView
    private lateinit var vNoMediaServersImage: ImageView
    private lateinit var vNoMediaServersText: TextView
    private lateinit var vAddMediaServer: Button

    private fun addOrEditMediaServer(mediaServer: MediaServer? = null) {
        val intent = Intent(this, EditMediaServerDetailsActivity::class.java)
        if (mediaServer != null)
            intent.putExtra("uid", mediaServer.uid)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_mediaservers)

        supportActionBar?.hide()

        vMediaServerList = findViewById(R.id.recyclerViewServer)
        vNoMediaServersImage = findViewById(R.id.empty_image_view)
        vNoMediaServersText = findViewById(R.id.empty_data_text)
        vAddMediaServer = findViewById(R.id.add_server)

        vAddMediaServer.setOnClickListener { addOrEditMediaServer() }

        val adapter = MediaServerAdapter { addOrEditMediaServer(it) }
        vMediaServerList.adapter = adapter

        GodObject.instance.db.mediaServerDao().getAllMediaServersLive().observe(this) {
            if (it == null || it.isEmpty()) {
                vNoMediaServersImage.visibility = View.VISIBLE
                vNoMediaServersText.visibility = View.VISIBLE
            } else {
                vNoMediaServersImage.visibility = View.GONE
                vNoMediaServersText.visibility = View.GONE
            }
            adapter.submitList(it)
        }
    }
}