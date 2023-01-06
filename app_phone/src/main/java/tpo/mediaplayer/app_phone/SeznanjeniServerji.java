package tpo.mediaplayer.app_phone;

import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SeznanjeniServerji extends AppCompatActivity
{
    RecyclerView recyclerViewServer;
    DBHelper myDB;
    ArrayList<String> server_id, username, path;
    CustomAdapterServers customAdapterServers;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seznanjeniserverji);

        recyclerViewServer = findViewById(R.id.recyclerViewServer);

        myDB = new DBHelper(SeznanjeniServerji.this);
        server_id = new ArrayList<>();
        username = new ArrayList<>();
        path = new ArrayList<>();

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().hide();
        }

        storeDataInArrays();

        customAdapterServers = new CustomAdapterServers(SeznanjeniServerji.this, this,  server_id, username, path);
        recyclerViewServer.setAdapter(customAdapterServers);
        recyclerViewServer.setLayoutManager(new LinearLayoutManager(SeznanjeniServerji.this));
    }

    void storeDataInArrays() {
        Cursor cursor = myDB.readAllDataServer();
        if(cursor.getCount() == 0) {
        } else {
            while (cursor.moveToNext()) {
                server_id.add(cursor.getString(0));
                username.add(cursor.getString(1));
                path.add(cursor.getString(3));
            }
        }
    }
}
