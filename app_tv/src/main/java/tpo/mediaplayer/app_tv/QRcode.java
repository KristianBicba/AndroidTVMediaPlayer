package tpo.mediaplayer.app_tv;

import tpo.mediaplayer.lib_communications.server.*;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import tpo.mediaplayer.lib_communications.shared.PairingData;


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
    private Handler mHandler= new Handler();
    Server server;




    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code);
        qrCodeIV = findViewById(R.id.idIVQrcode);
        textView = findViewById(R.id.iddeviceName);
        buttonNext = findViewById(R.id.idbutton);
        deviceListView = findViewById(R.id.iddevicelist);

        // start comunication server
        server = new Server(new ServerCallbacks() {
            @Override
            public void onOpen(@NonNull Server server) {

            }

            @Nullable
            @Override
            public String onPairingRequest(@NonNull String clientName, @NonNull String clientGuid) {
                Toast.makeText(getApplicationContext(),"Pairing", Toast.LENGTH_LONG).show();
                return null;
            }

            @Nullable
            @Override
            public String onConnectionRequest(@NonNull String clientGuid) {
                Toast.makeText(getApplicationContext(),"Conected", Toast.LENGTH_LONG).show();
                return null;
            }

            @Override
            public void onPlayRequest(@NonNull String connectionString) {

            }

            @Override
            public void onPauseRequest() {

            }

            @Override
            public void onResumeRequest() {

            }

            @Override
            public void onStopRequest() {

            }

            @Override
            public void onSeekRequest(long newTimeElapsed) {

            }

            @Override
            public void onClose(@Nullable Throwable error) {

            }
        });

        server.open();



        // open databese and load data
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "device-database")
                .allowMainThreadQueries().build();

        deviceDao = db.deviceDao();
        test_fill_devices_db(deviceDao);
        List<Device> devices = deviceDao.getAll();
        //deviceDao.delete(device);


        // load data into view
        loadIntoView(devices);



        //generate string for connection
        PairingData pairingData = server.beginPairing();


        String deviceName = Settings.Global.getString(getContentResolver(), "device_name");
        String connectionString = pairingData.toString(); //display the name of the tv


        // generate QRcode and desplay it

        textView.setText(deviceName);
        createQRcode(connectionString);




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
        if(deviceDao.getAll().size() == 0){
            for (int i = 0; i < 10; i++) {
                Device device = new Device();
                device.deviceName = "test" + i;
                device.communicationStr = "1232456765432-" + i;
                deviceDao.insert(device);
            }
        }
    }
}
