package tpo.mediaplayer.app_phone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.HashMap;

public class ConnectActivity extends AppCompatActivity {

    DBHelper myDB;
    Button connect;
    public static Television televizija;

    public static Television getTelevision(){
        return televizija;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        HashMap<String, Television> televizije = new HashMap<String, Television>();

        myDB = new DBHelper(ConnectActivity.this);

        Cursor cursor = myDB.readAllData();
        if(cursor.getCount() == 0) {
        } else {
            while (cursor.moveToNext()) {
                String ime = cursor.getString(1);
                String ip = cursor.getString(2);

                televizije.put(ime, new Television(ime, ip));
            }
        }

        connect = findViewById(R.id.buttonConnect1);

        String[] devices = myDB.getDevices();
        String[] servers = myDB.getServers();

        Spinner spin1 = (Spinner) findViewById(R.id.spinnerDevice);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, devices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin1.setAdapter(adapter);

        Spinner spin2 = (Spinner) findViewById(R.id.spinnerServer);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, servers);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin2.setAdapter(adapter2);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                televizija = televizije.get(spin1.getSelectedItem().toString());
                System.out.println(televizija.connect());
                System.out.println(televizija.connectToServer(spin2.getSelectedItem().toString()));
                startActivity(new Intent(ConnectActivity.this, FileSystemActivity.class));
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}