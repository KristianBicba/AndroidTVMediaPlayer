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

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private final Context context;
    Activity activity;
    private final ArrayList device_id;
    private final ArrayList device_name;
    private final ArrayList device_info;

    public CustomAdapter(Activity activity, Context context, ArrayList device_id, ArrayList device_name,
                   ArrayList device_info) {
        this.activity = activity;
        this.context = context;
        this.device_id = device_id;
        this.device_name = device_name;
        this.device_info = device_info;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.device_id.setText(String.valueOf(device_id.get(position)));
        holder.device_name.setText(String.valueOf(device_name.get(position)));
        holder.device_info.setText(String.valueOf(device_info.get(position)));

        holder.update_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UpdateActivity.class);
                intent.putExtra("id", String.valueOf(device_id.get(position)));
                intent.putExtra("name", String.valueOf(device_name.get(position)));
                intent.putExtra("info", String.valueOf(device_info.get(position)));
                activity.startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return device_id.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView device_id, device_name, device_info;
        LinearLayout update_layout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            device_id = itemView.findViewById(R.id.device_id);
            device_name = itemView.findViewById(R.id.device_name);
            device_info = itemView.findViewById(R.id.device_info);
            update_layout = itemView.findViewById(R.id.update_layout);
        }
    }
}
