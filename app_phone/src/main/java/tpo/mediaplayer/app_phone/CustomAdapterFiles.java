package tpo.mediaplayer.app_phone;

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

public class CustomAdapterFiles extends RecyclerView.Adapter<CustomAdapterFiles.MyViewHolderFiles>
{
    private Context context;
    Activity activity;
    private ArrayList files;
    private FileSystemActivity fileSystemActivity;

    CustomAdapterFiles(Activity activity, Context context, FileSystemActivity fileSystemActivity, ArrayList files)
    {
        this.context = context;
        this.files = files;
        this.activity = activity;
        this.fileSystemActivity = fileSystemActivity;
    }

    @NonNull
    @Override
    public MyViewHolderFiles onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row_file, parent, false);
        return new MyViewHolderFiles(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolderFiles holder, @SuppressLint("RecyclerView") int position) {
        holder.files.setText(String.valueOf(files.get(position)));

        holder.update_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String choosen = String.valueOf(files.get(position));
                if (fileSystemActivity.televizija.isDirectory(choosen)){
                    fileSystemActivity.televizija.cd(choosen);
                    Intent intent = new Intent(context, FileSystemActivity.class);
                    activity.startActivityForResult(intent, 1);
                } else {
                    System.out.println("film, prestavi na daljinec");
                    fileSystemActivity.televizija.currentMovie = choosen;
                    Intent intent = new Intent(context, RemoteControlActivity.class);
                    activity.startActivityForResult(intent, 1);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class MyViewHolderFiles extends RecyclerView.ViewHolder{

        TextView files;
        LinearLayout update_layout;

        public MyViewHolderFiles(@NonNull View itemView) {
            super(itemView);
            files = itemView.findViewById(R.id.file_name);
            update_layout = itemView.findViewById(R.id.update_layout);
        }
    }
}

