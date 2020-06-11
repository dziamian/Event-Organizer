package com.example.eventorganizer;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import network_structures.EventInfoUpdate;
import network_structures.SectorInfoFixed;
import network_structures.SectorInfoUpdate;

public class SectorLayout extends ItemLayout {

    private final SectorInfoFixed sectorInfoFixed;
    private SectorLayoutHolder sectorLayoutHolder;

    public SectorLayout(SectorInfoFixed sectorInfoFixed) {
        super(R.layout.sector_item);
        this.sectorInfoFixed = sectorInfoFixed;
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

        sectorLayoutHolder.textViewName.setText(sectorInfoFixed.getName());
        sectorLayoutHolder.textViewAddress.setText(sectorInfoFixed.getAddress());
        sectorLayoutHolder.textViewAvailableRooms.setText("Aktywne atrakcje: " + sectorInfoFixed.getRooms().size());

        view.findViewById(R.id.sector_field).setOnClickListener(v -> {
            ((HomeActivity) context).setRoomActivity(sectorInfoFixed.getId());
        });
    }

    public void updateLayout(EventInfoUpdate eventInfoUpdate) {
        sectorLayoutHolder.textViewAvailableRooms.setText("Aktywne atrakcje: " + eventInfoUpdate.getSectors().get(sectorInfoFixed.getId()).getActiveRooms());
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
