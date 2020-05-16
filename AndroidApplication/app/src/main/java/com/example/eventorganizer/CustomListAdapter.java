package com.example.eventorganizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import network_structures.SectorInfo;

import java.util.ArrayList;

public class CustomListAdapter extends BaseAdapter {

    public ArrayList<SectorInfo> sectorLayoutList;
    public LayoutInflater layoutInflater;

    public CustomListAdapter(Context context, ArrayList<SectorInfo> sectorLayoutList) {
        this.sectorLayoutList = sectorLayoutList;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return sectorLayoutList.size();
    }

    @Override
    public Object getItem(int position) {
        return sectorLayoutList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.sector_item, null);

            viewHolder = new ViewHolder();
            viewHolder.textViewName = convertView.findViewById(R.id.sector_name);
            viewHolder.textViewAddress = convertView.findViewById(R.id.sector_address);
            viewHolder.textViewAvailableRooms = convertView.findViewById(R.id.sector_available_rooms);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //convertView.setPadding(100, 100, 100, 0);

        SectorInfo sectorLayout = sectorLayoutList.get(position);

        viewHolder.textViewName.setText(sectorLayout.name);
        viewHolder.textViewAddress.setText(sectorLayout.address);
        viewHolder.textViewAvailableRooms.setText("Aktywne atrakcje: " + 0);

        return convertView;
    }

    static class ViewHolder {
        TextView textViewName, textViewAddress, textViewAvailableRooms;
    }
}
