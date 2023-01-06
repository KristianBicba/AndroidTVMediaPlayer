package tpo.mediaplayer.app_phone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddServer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_server);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        EditText usernameInput = findViewById(R.id.textInputUsername);
        EditText passwordInput = findViewById(R.id.textInputPassword);
        EditText pathInput = findViewById(R.id.textInputPath);

        Button buttonAddServer = findViewById(R.id.buttonAddServer);

        buttonAddServer.setOnClickListener(v ->
        {
            if (!TextUtils.isEmpty(usernameInput.getText()) &&!TextUtils.isEmpty(passwordInput.getText()) &&!TextUtils.isEmpty(pathInput.getText())) {
                DBHelper database = new DBHelper(AddServer.this);
                database.addServer(usernameInput.getText().toString(), passwordInput.getText().toString(), pathInput.getText().toString());
                Toast.makeText(getApplicationContext(), "Added Server", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Missing Fields", Toast.LENGTH_LONG).show();
            }
        });
    }
}