package tpo.mediaplayer.app_tv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class QRcode extends AppCompatActivity {


    private ImageView qrCodeIV;
    private TextView textView;
    private Button buttonNext;
    private RecyclerView deviceListView;
    DeviceDao deviceDao;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    List<Device> toBeDeleted = new ArrayList<Device>();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code);
        qrCodeIV = findViewById(R.id.idIVQrcode);
        textView = findViewById(R.id.iddeviceName);
        buttonNext = findViewById(R.id.idbutton);
        deviceListView = findViewById(R.id.iddevicelist);


        // open databese and load data
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "device-database")
                .allowMainThreadQueries().build();

        deviceDao = db.deviceDao();
        //test_fill_devices_db(deviceDao);
        List<Device> devices = deviceDao.getAll();
        //deviceDao.delete(device);
        // load data into view

        loadIntoView(devices);



        //generate string for connection

        String deviceName = Settings.Global.getString(getContentResolver(), "device_name");
        String connectionString = "con://testuser:testpassword@10.0.2.2" + deviceName; //display the name of the tv


        // generate QRcode and desplay it

        textView.setText(deviceName);
        createQRcode(connectionString);













        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFromDatabase();
                switchActivities();
            }
        });

    }


    private void createQRcode(String connectionString){
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int dimen = width < height ? width : height;
        dimen = dimen * 3 / 4;

        qrgEncoder = new QRGEncoder(connectionString, null, QRGContents.Type.TEXT, dimen);
        try {
            bitmap = qrgEncoder.encodeAsBitmap();
            qrCodeIV.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.e("Tag", e.toString());
        }
    }
    private void loadIntoView(List<Device> devices){
        Device [] devicelist = new Device[devices.size()];
        for (int i = 0; i < devices.size(); i++) {
            devicelist[i] = devices.get(i);
        }
        DeviceListAdapter adapter = new DeviceListAdapter(devicelist, toBeDeleted);
        deviceListView.setHasFixedSize(true);
        deviceListView.setLayoutManager(new LinearLayoutManager(this));
        deviceListView.setAdapter(adapter);
    }

    private void switchActivities() {
        Intent switchActivityIntent = new Intent(this, VideoPlayer.class);
        startActivity(switchActivityIntent);
    }
    private void deleteFromDatabase(){
        if(toBeDeleted.size() < 1)
            return;
        for (int i = 0; i < toBeDeleted.size(); i++) {
            deviceDao.delete(toBeDeleted.get(i));
        }

    }

    private void test_fill_devices_db(DeviceDao deviceDao){
        for (int i = 0; i < 10; i++) {
            Device device = new Device();
            device.deviceName = "test" + i;
            device.communicationStr = "1232456765432-" + i;
            deviceDao.insert(device);
        }
    }
}
