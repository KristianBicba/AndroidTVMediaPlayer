package tpo.mediaplayer.app_phone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.net.Socket;

import tpo.mediaplayer.app_phone.DBHelper;
import tpo.mediaplayer.app_phone.HexUtilKt;
import tpo.mediaplayer.app_phone.R;
import tpo.mediaplayer.lib_communications.client.ClientPairingHelper;
import tpo.mediaplayer.lib_communications.shared.PairingData;

public class MainActivity extends AppCompatActivity {

    Button dodaj_napravo_scan;
    Button dodaj_streznik_scan;
    Button vse_naprave;
    Button vsi_strezniki;
    Button connect;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mainHandler = new Handler(getMainLooper());

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
        vse_naprave.setOnClickListener(l -> {
            Intent vse_naprave_intent = new Intent(this, SeznanjeneNaprave.class);
            startActivity(vse_naprave_intent);
        });

        vsi_strezniki.setOnClickListener(l -> {
            Intent vsi_serverji_intent = new Intent(this, SeznanjeniServerji.class);
            startActivity(vsi_serverji_intent);
        });

        connect.setOnClickListener(l -> {
            startActivity(new Intent(MainActivity.this, ConnectActivity.class));
        });
    }

    private void scanNaprava() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to use flash");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);

        barLauncher1.launch(options);
    }

    private void attemptPairing(PairingData data) {
        System.out.println("Got pairing data!");
        new Thread(() -> {
            ClientPairingHelper result =
                    ClientPairingHelper.attemptToPair(data, "Android", "1234");
            mainHandler.post(() -> {
                if (result == null) {
                    Toast.makeText(getApplicationContext(), "Povezava neuspešna", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(), "Povezava uspešna", Toast.LENGTH_SHORT)
                            .show();
                    DBHelper database = new DBHelper(MainActivity.this);
                    database.addDevice(
                            result.getName(),
                            HexUtilKt.hexEncode(result.getAddress().getAddress())
                    );
                }
            });
        }).start();
    }

    ActivityResultLauncher<ScanOptions> barLauncher1 = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            String scanned = result.getContents();
            System.out.println(scanned);
            byte[] decoded = HexUtilKt.hexDecode(scanned);
            if (decoded != null) {
                PairingData data = PairingData.fromByteArray(decoded);
                if (data != null) {
                    attemptPairing(data);
                    return;
                }
            }
        }
        Toast.makeText(getApplicationContext(), "Narobe skenirana naprava", Toast.LENGTH_SHORT).show();
    });
}