package tpo.mediaplayer.app_phone.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface MediaServerDao {
    @Query("SELECT * FROM MediaServer WHERE uid = :uid")
    fun getMediaServerByUid(uid: Int): MediaServer?

    @Query("SELECT * FROM MediaServer")
    fun getAllMediaServers(): List<MediaServer>

    @Query("SELECT * FROM MediaServer")
    fun getAllMediaServersLive(): LiveData<List<MediaServer>>

    @Insert
    fun insertMediaServer(mediaServer: MediaServer)

    @Upsert
    fun upsertMediaServer(mediaServer: MediaServer)

    @Query("DELETE FROM MediaServer WHERE uid = :uid")
    fun deleteMediaServerByUid(uid: Int)
}