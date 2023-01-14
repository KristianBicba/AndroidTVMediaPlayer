package tpo.mediaplayer.app_phone.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import tpo.mediaplayer.app_phone.GodObject
import tpo.mediaplayer.app_phone.R
import tpo.mediaplayer.app_phone.db.MediaServer

class EditMediaServerDetailsActivity : AppCompatActivity() {
    private lateinit var vScreenTitle: TextView
    private lateinit var vButtonCommit: Button
    private lateinit var vButtonRemove: Button
    private lateinit var vEditName: EditText
    private lateinit var vEditConnectionString: EditText

    private var uid: Int? = null

    private fun adjustIfEditing() {
        val uid = intent.getIntExtra("uid", 0)
        if (uid == 0) return

        val mediaServer = GodObject.instance.db.mediaServerDao().getMediaServerByUid(uid) ?: return
        this.uid = uid
        vScreenTitle.text = getString(R.string.edit_mediaserver_details_title_edit)
        vButtonCommit.text = getString(R.string.edit_mediaserver_details_button_commit_edit)
        vButtonRemove.visibility = View.VISIBLE
        vEditName.setText(mediaServer.name)
        vEditConnectionString.setText(mediaServer.connectionString)
    }

    private fun onClickCommit() {
        val entity = MediaServer(uid ?: 0, vEditName.text.toString(), vEditConnectionString.text.toString())
        GodObject.instance.db.mediaServerDao().upsertMediaServer(entity)
        finish()
    }

    private fun onClickRemove() {
        GodObject.instance.db.mediaServerDao().deleteMediaServerByUid(uid ?: return)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_mediaserver_details)

        supportActionBar?.hide()

        vScreenTitle = findViewById(R.id.textView4)
        vButtonCommit = findViewById(R.id.buttonAddServer)
        vButtonRemove = findViewById(R.id.buttonRemoveServer)
        vEditName = findViewById(R.id.textInputName)
        vEditConnectionString = findViewById(R.id.textInputConnectionString)

        adjustIfEditing()

        vButtonCommit.setOnClickListener { onClickCommit() }
        vButtonRemove.setOnClickListener { onClickRemove() }
    }
}