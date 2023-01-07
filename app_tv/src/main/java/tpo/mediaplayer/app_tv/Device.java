package tpo.mediaplayer.app_tv;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Device {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "device_name")
    public String deviceName;

    @ColumnInfo(name = "communication_str")
    public String communicationStr;
}
