package tpo.mediaplayer.app_tv.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Device.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DeviceDao deviceDao();
}
