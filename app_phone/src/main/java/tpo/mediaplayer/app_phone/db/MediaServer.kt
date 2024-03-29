package tpo.mediaplayer.app_phone.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MediaServer(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "connection_string") val connectionString: String
)
