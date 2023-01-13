package tpo.mediaplayer.app_phone.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import tpo.mediaplayer.app_phone.recyclerViewAdapter.CustomAdapter;
import tpo.mediaplayer.app_phone.DBHelper;
import tpo.mediaplayer.app_phone.R;

public class EditPairedDevicesActivity extends AppCompatActivity
{
    RecyclerView recyclerView;

    DBHelper myDB;
    ArrayList<String> device_id, device_name, device_info;
    CustomAdapter customAdapter;
    ImageView empty_image;
    TextView empty_msg;
    Button add_device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_paired_devices);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.recyclerView);

        myDB = new DBHelper(EditPairedDevicesActivity.this);
        device_id = new ArrayList<>();
        device_name = new ArrayList<>();
        device_info = new ArrayList<>();

        empty_image = findViewById(R.id.empty_image_view);
        empty_msg = findViewById(R.id.empty_data_text);

        add_device = findViewById(R.id.add_device);
        add_device.setOnClickListener(v ->
        {
            scanNaprava();
        });
        storeDataInArrays();

        customAdapter = new CustomAdapter(EditPairedDevicesActivity.this, this, device_id, device_name, device_info);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(EditPairedDevicesActivity.this));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1)
        {
            recreate();
        }
    }

    public void storeDataInArrays() {
        Cursor cursor = myDB.readAllData();
        if(cursor.getCount() == 0) {
            empty_image.setVisibility(View.VISIBLE);
            empty_msg.setVisibility(View.VISIBLE);
        } else {
            while (cursor.moveToNext()) {
                device_id.add(cursor.getString(0));
                device_name.add(cursor.getString(1));
                device_info.add(cursor.getString(2));
            }
            empty_image.setVisibility(View.GONE);
            empty_msg.setVisibility(View.GONE);
        }
    }

    private void scanNaprava()
    {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to use flash");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);

        barLauncher1.launch(options);
    }
    ActivityResultLauncher<ScanOptions> barLauncher1 = registerForActivityResult(new ScanContract(), result -> {

        if(result.getContents() != null && result.getContents().startsWith("naprava")) {
//            DBHelper database = new DBHelper(SeznanjeneNaprave.this);
//            database.addDevice(result.getContents(), result.getContents());
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Narobe skenirana naprava", Toast.LENGTH_SHORT).show();
        }
        recreate();
    });
}
