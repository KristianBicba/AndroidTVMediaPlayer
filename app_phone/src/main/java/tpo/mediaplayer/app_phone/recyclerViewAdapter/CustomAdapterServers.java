package tpo.mediaplayer.app_phone.recyclerViewAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import tpo.mediaplayer.app_phone.R;
import tpo.mediaplayer.app_phone.activity.UpdateActivity;

public class CustomAdapterServers extends RecyclerView.Adapter<CustomAdapterServers.MyViewHolderServers>
{
    private final Context context;
    Activity activity;
    private final ArrayList server_id;
    private final ArrayList username;
    private final ArrayList path;


    public CustomAdapterServers(Activity activity, Context context, ArrayList server_id, ArrayList username,
                             ArrayList path)
    {
        this.context = context;
        this.server_id = server_id;
        this.username = username;
        this.path = path;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolderServers onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row_servers, parent, false);

        return new MyViewHolderServers(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolderServers holder, @SuppressLint("RecyclerView") int position) {
        holder.server_id.setText(String.valueOf(server_id.get(position)));
        holder.username.setText(String.valueOf(username.get(position)));
        holder.path.setText(String.valueOf(path.get(position)));

        holder.update_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UpdateActivity.class);
                intent.putExtra("id", String.valueOf(server_id.get(position)));
                intent.putExtra("name", String.valueOf(username.get(position)));
                intent.putExtra("info", String.valueOf(path.get(position)));
                activity.startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return server_id.size();
    }

    public class MyViewHolderServers extends RecyclerView.ViewHolder{

        TextView server_id, username, path;
        LinearLayout update_layout;

        public MyViewHolderServers(@NonNull View itemView) {
            super(itemView);
            server_id = itemView.findViewById(R.id.server_id);
            username = itemView.findViewById(R.id.username);
            path = itemView.findViewById(R.id.path);
            update_layout = itemView.findViewById(R.id.update_layout);
        }
    }
}
