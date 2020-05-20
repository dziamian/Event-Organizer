package com.example.eventorganizer;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import network_structures.SectorInfo;
import network_structures.SectorUpdate;

public class SectorLayout extends ItemLayout {

    private final SectorInfo sectorInfo;
    public SectorLayoutHolder sectorLayoutHolder;

    public SectorLayout(SectorInfo sectorInfo) {
        super(R.layout.sector_item);
        this.sectorInfo = sectorInfo;
        this.sectorLayoutHolder = new SectorLayoutHolder();
    }

    @Override
    protected Object getItemHolder() {
        return sectorLayoutHolder;
    }

    @Override
    public void setLayout(View view, Context context) {
        sectorLayoutHolder.textViewName = view.findViewById(R.id.sector_name);
        sectorLayoutHolder.textViewAddress = view.findViewById(R.id.sector_address);
        sectorLayoutHolder.textViewAvailableRooms = view.findViewById(R.id.sector_available_rooms);

        Log.d("LOG", "wtf " + sectorInfo.name);

        sectorLayoutHolder.textViewName.setText(sectorInfo.name);
        sectorLayoutHolder.textViewAddress.setText(sectorInfo.address);
        sectorLayoutHolder.textAvailableRooms = "Aktywne atrakcje: ";
        sectorLayoutHolder.textViewAvailableRooms.setText(sectorLayoutHolder.textAvailableRooms + sectorInfo.rooms.size());

        view.findViewById(R.id.sector_field).setOnClickListener(v -> {
            ((HomeActivity) context).setRoomActivity(sectorInfo.id);
        });
    }

    public void updateLayout(SectorUpdate update) {
        if (update != null) {
            Log.d("LOG2", "WTF " + sectorInfo.name + " " + update.currentActive);
            sectorLayoutHolder.textViewAvailableRooms.setText(sectorLayoutHolder.textAvailableRooms + update.currentActive);
        }
    }

    @Override
    public void setItemHolder(Object itemHolder) {
        if (itemHolder instanceof SectorLayoutHolder) {
            sectorLayoutHolder = (SectorLayoutHolder) itemHolder;
        }
    }

    public static class SectorLayoutHolder extends ItemHolder {
        String textAvailableRooms;
        TextView textViewName, textViewAddress, textViewAvailableRooms;
    }
}
