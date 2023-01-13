package tpo.mediaplayer.app_phone.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DeviceDao {
    @Query("SELECT * FROM Device WHERE uid = :uid")
    fun getDeviceByUid(uid: Int): Device?

    @Query("SELECT * FROM Device")
    fun getAllDevices(): List<Device>

    @Insert
    fun insertDevice(device: Device)

    @Query("DELETE FROM Device WHERE uid = :uid")
    fun deleteDeviceByUid(uid: Int)
}