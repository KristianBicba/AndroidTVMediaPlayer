package tpo.mediaplayer.app_phone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    Button dodaj_napravo_scan;
    Button dodaj_streznik_scan;
    Button vse_naprave;
    Button vsi_strezniki;
    Button connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //VSI ORIGINALNI GUMBI
        dodaj_napravo_scan = findViewById(R.id.dodaj_napravo);
        dodaj_streznik_scan = findViewById(R.id.dodaj_streznik);
        vse_naprave = findViewById(R.id.seznanjene_naprave);
        vsi_strezniki = findViewById(R.id.seznanjeni_strezniki);
        connect = findViewById(R.id.buttonConnect);

        //SKENIRANJE
        dodaj_napravo_scan.setOnClickListener(v ->
        {
            scanNaprava();
        });

        dodaj_streznik_scan.setOnClickListener(v ->
        {
            startActivity(new Intent(MainActivity.this, AddServer.class));
        });

        //SEZNANNJENE NAPRAVE GUMB
        vse_naprave.setOnClickListener(l ->{
            Intent vse_naprave_intent = new Intent(this, SeznanjeneNaprave.class);
            startActivity(vse_naprave_intent);
        });

        vsi_strezniki.setOnClickListener(l ->{
            Intent vsi_serverji_intent = new Intent(this, SeznanjeniServerji.class);
            startActivity(vsi_serverji_intent);
        });

        connect.setOnClickListener(l ->{
            startActivity(new Intent(MainActivity.this, ConnectActivity.class));
        });
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
            DBHelper database = new DBHelper(MainActivity.this);
            database.addDevice(result.getContents(), result.getContents());
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Narobe skenirana naprava", Toast.LENGTH_SHORT).show();
        }
    });
}