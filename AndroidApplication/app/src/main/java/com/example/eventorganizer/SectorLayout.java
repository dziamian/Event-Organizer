package com.example.eventorganizer;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import network_structures.SectorInfo;

public class SectorLayout extends ItemLayout {

    private final SectorInfo sectorInfo;
    private SectorLayoutHolder sectorLayoutHolder;

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

        sectorLayoutHolder.textViewName.setText(sectorInfo.name);
        sectorLayoutHolder.textViewAddress.setText(sectorInfo.address);
        sectorLayoutHolder.textViewAvailableRooms.setText("Aktywne atrakcje: " + sectorInfo.rooms.size());

        view.findViewById(R.id.sector_field).setOnClickListener(v -> {
            ((HomeActivity) context).setRoomActivity(sectorInfo.id);
        });
    }

    @Override
    public void setItemHolder(Object itemHolder) {
        if (itemHolder instanceof SectorLayoutHolder) {
            sectorLayoutHolder = (SectorLayoutHolder) itemHolder;
        }
    }

    private static class SectorLayoutHolder extends ItemHolder {
        TextView textViewName, textViewAddress, textViewAvailableRooms;
    }
}
