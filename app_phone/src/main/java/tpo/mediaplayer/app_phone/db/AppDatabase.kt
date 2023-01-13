package tpo.mediaplayer.app_phone.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MediaServer::class, Device::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun mediaServerDao(): MediaServerDao
}