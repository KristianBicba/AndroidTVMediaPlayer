package tpo.mediaplayer.app_phone.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import tpo.mediaplayer.app_phone.DBHelper;
import tpo.mediaplayer.app_phone.HexUtilKt;
import tpo.mediaplayer.app_phone.R;
import tpo.mediaplayer.app_phone.Television;

public class ConnectActivity extends AppCompatActivity {

    DBHelper myDB;
    Button connect;
    public static Television televizija;

    public static Television getTelevision() {
        return televizija;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        HashMap<String, Television> televizije = new HashMap<String, Television>();
        HashMap<String, InetAddress> serverIps = new HashMap<>();

        myDB = new DBHelper(ConnectActivity.this);

        Cursor cursor = myDB.readAllData();
        if (cursor.getCount() == 0) {
        } else {
            while (cursor.moveToNext()) {
                String ime = cursor.getString(1);
                String ip = cursor.getString(2);

                byte[] addrBytes = HexUtilKt.hexDecode(ip);
                if (addrBytes == null) continue;
                try {
                    InetAddress address = InetAddress.getByAddress(addrBytes);
                    serverIps.put(ime, address);
                } catch (IOException fuckyou) {
                }

//                televizije.put(ime, new Television(ime, ip));
            }
        }

        connect = findViewById(R.id.buttonConnect1);

        String[] devices = myDB.getDevices();
        String[] servers = myDB.getServers();

        Spinner spin1 = findViewById(R.id.spinnerDevice);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, devices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin1.setAdapter(adapter);

        Spinner spin2 = findViewById(R.id.spinnerServer);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, servers);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin2.setAdapter(adapter2);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InetAddress address = serverIps.get(spin1.getSelectedItem().toString());
                String connString = spin2.getSelectedItem().toString();

                startActivity(new Intent(ConnectActivity.this, FileSystemActivity.class));
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}