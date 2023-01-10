package tpo.mediaplayer.app_tv;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {
    private Device[] listdata;
    private boolean selected = false;
    private List<Device> toBeDeleted;

    public DeviceListAdapter(Device[] listdata, List<Device> toBeDeleted) {
        this.listdata = listdata;
        this.toBeDeleted = toBeDeleted;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.list_item, parent, false);
        listItem.setFocusable(true);
        listItem.setFocusableInTouchMode(true);
        ViewHolder viewHolder = new ViewHolder(listItem);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Device device = listdata[position];
        holder.textView.setText(listdata[position].deviceName);
        holder.relativeLayout.setOnClickListener(view -> {
            selected = !selected;
            if (selected) {
                holder.textView.setTextColor(Color.parseColor("#D10000"));
                toBeDeleted.add(device);
            } else {
                holder.textView.setTextColor(Color.parseColor("#FFFFFF"));
                toBeDeleted.remove(device);
            }
            System.out.println(toBeDeleted.toString());
        });
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.textView.setBackgroundColor(Color.parseColor("#FFBB86FC"));
            } else {
                holder.textView.setBackgroundColor(Color.parseColor("#FF3700B3"));
            }
        });
    }

    @Override
    public int getItemCount() {
        return listdata.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.textView = (TextView) itemView.findViewById(R.id.textView);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);
        }
    }
}


