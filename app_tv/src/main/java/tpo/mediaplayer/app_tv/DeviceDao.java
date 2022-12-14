package tpo.mediaplayer.app_tv;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeviceDao {
    @Query("SELECT * FROM device")
    List<Device> getAll();

    @Query("SELECT * FROM device WHERE communication_str = :id")
    Device getByGuid(String id);

    @Insert
    void insert(Device device);

    @Delete
    void delete(Device device);
}
