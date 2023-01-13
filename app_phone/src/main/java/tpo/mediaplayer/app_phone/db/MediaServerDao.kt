package tpo.mediaplayer.app_phone.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MediaServerDao {
    @Query("SELECT * FROM MediaServer WHERE uid = :uid")
    fun getMediaServerByUid(uid: Int): MediaServer?

    @Query("SELECT * FROM MediaServer")
    fun getAllMediaServers(): List<MediaServer>

    @Insert
    fun insertMediaServer(mediaServer: MediaServer)

    @Query("DELETE FROM Device WHERE uid = :uid")
    fun deleteMediaServerByUid(uid: Int)
}