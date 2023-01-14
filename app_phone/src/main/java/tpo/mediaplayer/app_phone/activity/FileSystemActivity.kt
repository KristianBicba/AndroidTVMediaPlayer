package tpo.mediaplayer.app_phone.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import tpo.mediaplayer.app_phone.R
import tpo.mediaplayer.app_phone.recyclerViewAdapter.FileSystemAdapter
import tpo.mediaplayer.lib_vfs.DefaultFileSystemFactory
import tpo.mediaplayer.lib_vfs.VfsDirEntry
import tpo.mediaplayer.lib_vfs.VfsFileSystem
import java.io.IOException

class FileSystemActivity : AppCompatActivity() {
    private val handler by lazy { Handler(mainLooper) }

    private lateinit var vPath: TextView
    private lateinit var vError: TextView
    private lateinit var vFiles: RecyclerView

    private lateinit var intentConnectionString: String
    private lateinit var intentPath: String
    private var intentServerUid: Int = 0

    private var stopped = false
        @Synchronized get
        @Synchronized set
    private lateinit var adapter: FileSystemAdapter

    private var vfs: VfsFileSystem? = null

    private fun closeVfs() {
        vfs?.close()
        vfs = null
    }

    private fun updateLoading() {
        vError.visibility = View.GONE
        vFiles.visibility = View.VISIBLE
        adapter.submitList(listOf())
    }

    private fun updateErrorConnect(error: String) {
        vError.visibility = View.VISIBLE
        vFiles.visibility = View.GONE
        vError.text = error
    }

    private fun updateSuccess(dirEntries: List<VfsDirEntry>) {
        adapter.submitList(dirEntries)
        closeVfs()
    }

    private fun updateErrorOpenRequest() {
        vError.visibility = View.VISIBLE
        vPath.visibility = View.GONE
        vError.text = getString(R.string.file_system_error_open)
        closeVfs()
    }

    private fun updateErrorClosedRequest() {
        vError.visibility = View.VISIBLE
        vPath.visibility = View.GONE
        vError.text = getString(R.string.file_system_error_closed)
    }

    private fun initializeVfs() {
        val vfs = try {
            DefaultFileSystemFactory.build(intentConnectionString)
        } catch (e: IOException) {
            handler.post { updateErrorConnect(e.toString()) }
            return
        }
        if (vfs == null) {
            handler.post { updateErrorConnect("Invalid connection string") }
            return
        }
        this.vfs = vfs
        if (stopped) return vfs.close()
        val result = vfs.ls(intentPath)
        if (result != null) {
            handler.post { updateSuccess(result) }
            return
        }
        if (vfs.isClosed) {
            this.vfs = null
            handler.post { updateErrorClosedRequest() }
        } else {
            handler.post { updateErrorOpenRequest() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_system)

        vPath = findViewById(R.id.file_system_text_path)
        vError = findViewById(R.id.file_system_text_error)
        vFiles = findViewById(R.id.file_system_list)

        intentConnectionString = intent.getStringExtra("connection_string") ?: return finish()
        intentPath = intent.getStringExtra("path") ?: return finish()
        intentServerUid = intent.getIntExtra("server_uid", 0).also {
            if (it == 0) return finish()
        }

        vPath.text = intentPath

        adapter = FileSystemAdapter {
            if (it.isDirectory) {
                val intent = Intent(this, FileSystemActivity::class.java).apply {
                    putExtra("connection_string", intentConnectionString)
                    putExtra("path", it.path)
                    putExtra("server_uid", intentServerUid)
                }
                startActivity(intent)
            } else {
                val intent = Intent(this, RemoteControlActivity::class.java).apply {
                    putExtra("server_uid", intentServerUid)
                    putExtra("connection_string_path", it.connectionString)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
            }
        }
        vFiles.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        stopped = false
        updateLoading()
        Thread { initializeVfs() }.start()
    }

    override fun onStop() {
        super.onStop()
        stopped = true
        closeVfs()
    }
}