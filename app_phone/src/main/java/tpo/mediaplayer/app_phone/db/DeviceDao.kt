package tpo.mediaplayer.app_phone.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DeviceDao {
    @Query("SELECT * FROM Device WHERE uid = :uid")
    fun getDeviceByUid(uid: Int): Device?

    @Query("SELECT * FROM Device")
    fun getAllDevices(): List<Device>

    @Query("SELECT * FROM Device")
    fun getAllDevicesLive(): LiveData<List<Device>>

    @Insert
    fun insertDevice(device: Device)

    @Query("DELETE FROM Device WHERE uid = :uid")
    fun deleteDeviceByUid(uid: Int)
}