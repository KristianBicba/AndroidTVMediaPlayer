package tpo.mediaplayer.app_phone.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import tpo.mediaplayer.app_phone.DBHelper;
import tpo.mediaplayer.app_phone.R;

public class UpdateActivity extends AppCompatActivity {

    EditText device_id, device_name, device_info;
    Button update_button, delete_button;
    String id, name, info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_naprava);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        device_id = findViewById(R.id.id_naprava_update);
        device_name = findViewById(R.id.name_naprava_update);
        device_info = findViewById(R.id.info_naprava_update2);

        update_button = findViewById(R.id.update_button);
        delete_button = findViewById(R.id.delete_button);


        //First we call this
        getAndSetIntentData();

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(name);
        }


        update_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //And only then we call this
                DBHelper database = new DBHelper(UpdateActivity.this);
                id = device_id.getText().toString().trim();
                name = device_name.getText().toString().trim();
                info = device_info.getText().toString().trim();
                database.updateData(id, name, info);
            }
        });

        delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDialog();
            }
        });

    }

    private void getAndSetIntentData()
    {
        if(getIntent().hasExtra("id") && getIntent().hasExtra("name") && getIntent().hasExtra("info"))
        {
            //GETTING INTENT DATA
            id = getIntent().getStringExtra("id");
            name = getIntent().getStringExtra("name");
            info = getIntent().getStringExtra("info");

            //SETTING INTENT DATA
            device_id.setText(id);
            device_name.setText(name);
            device_info.setText(info);
        }
        else
        {
            Toast.makeText(this, "No data.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete this device");
        builder.setMessage("Are you sure you want to delete this device?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DBHelper database = new DBHelper(UpdateActivity.this);
                database.deleteOneRow(id);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }
}