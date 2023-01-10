package tpo.mediaplayer.app_tv.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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

import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import tpo.mediaplayer.app_tv.AppDatabase;
import tpo.mediaplayer.app_tv.Device;
import tpo.mediaplayer.app_tv.DeviceDao;
import tpo.mediaplayer.app_tv.DeviceListAdapter;
import tpo.mediaplayer.app_tv.GodObject;
import tpo.mediaplayer.app_tv.HexUtilKt;
import tpo.mediaplayer.app_tv.R;
import tpo.mediaplayer.app_tv.service.MainServerService;
import tpo.mediaplayer.app_tv.service.VideoPlayerLauncherService;


//this is dumb but


public class QRcode extends AppCompatActivity {


    private ImageView qrCodeIV;
    private TextView textView;
    private Button buttonNext;
    private RecyclerView deviceListView;
    DeviceDao deviceDao;
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    List<Device> toBeDeleted = new ArrayList<Device>();
    private Handler mHandler = new Handler();

    private MainServerService.LocalBinder binder = null;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MainServerService.LocalBinder) service;
            byte[] pairingData = binder.setPairing(true);
            if (pairingData != null) {
                String hexPairingData = HexUtilKt.hexEncode(pairingData);
                createQRcode(hexPairingData);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder.setPairing(false);
            binder = null;
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code);

        Intent intent = new Intent(this, VideoPlayerLauncherService.class);
        startService(intent);

        Intent bindIntent = new Intent(this, MainServerService.class);
        bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);

        qrCodeIV = findViewById(R.id.idIVQrcode);
        textView = findViewById(R.id.iddeviceName);
        buttonNext = findViewById(R.id.idbutton);
        deviceListView = findViewById(R.id.iddevicelist);


        // open databese and load data
        AppDatabase db = GodObject.INSTANCE.getDb();

        deviceDao = db.deviceDao();
        test_fill_devices_db(deviceDao);
        List<Device> devices = deviceDao.getAll();
        //deviceDao.delete(device);


        // load data into view
        loadIntoView(devices);


        String deviceName = Settings.Global.getString(getContentResolver(), "device_name");
//        String connectionString = pairingData.toString(); //display the name of the tv
//        String connectionString = "example example example";


        // generate QRcode and desplay it

        textView.setText(deviceName);
//        createQRcode(connectionString);


        // start video player


        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFromDatabase();
                switchActivities();
            }
        });
        //set focus
        mHandler.post(new Runnable() {
            public void run() {
                deviceListView.requestFocus();
            }
        });

    }


    private void createQRcode(String connectionString) {
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

    private void loadIntoView(List<Device> devices) {
        Device[] devicelist = new Device[devices.size()];
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

    private void deleteFromDatabase() {
        if (toBeDeleted.size() < 1)
            return;
        for (int i = 0; i < toBeDeleted.size(); i++) {
            deviceDao.delete(toBeDeleted.get(i));
        }

    }

    private void test_fill_devices_db(DeviceDao deviceDao) {
        if (deviceDao.getAll().size() == 0) {
            for (int i = 0; i < 10; i++) {
                Device device = new Device();
                device.deviceName = "test" + i;
                device.communicationStr = "1232456765432-" + i;
                deviceDao.insert(device);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
