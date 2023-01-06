package tpo.mediaplayer.app_phone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class FileSystemActivity extends AppCompatActivity {

    static Television televizija = ConnectActivity.getTelevision();
    RecyclerView recyclerViewFiles;
    CustomAdapterFiles customAdapterFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_system);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        System.out.println(Arrays.toString(televizija.files.toArray()));

        recyclerViewFiles = findViewById(R.id.recyclerViewFiles);
        ArrayList<String> files = televizija.files;

        Collections.sort(files, String.CASE_INSENSITIVE_ORDER);

        customAdapterFiles = new CustomAdapterFiles(FileSystemActivity.this, this, this, files);
        recyclerViewFiles.setAdapter(customAdapterFiles);
        recyclerViewFiles.setLayoutManager(new LinearLayoutManager(FileSystemActivity.this));

        Button buttonBack = findViewById(R.id.buttonBackFiles);

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FileSystemActivity.this, MainActivity.class));
            }
        });

    }
}