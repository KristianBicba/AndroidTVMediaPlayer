package tpo.mediaplayer.app_tv.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeviceDao {
    @Query("SELECT * FROM Device")
    List<Device> getAll();

    @Query("SELECT * FROM Device")
    LiveData<List<Device>> getAllLive();

    @Query("SELECT * FROM Device WHERE communication_str = :id")
    Device getByGuid(String id);

    @Query("SELECT * FROM Device WHERE uid = :uid")
    Device getByUid(int uid);

    @Insert
    void insert(Device device);

    @Delete
    void delete(Device device);
}
