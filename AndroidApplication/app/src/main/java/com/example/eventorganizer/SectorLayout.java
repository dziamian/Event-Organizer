package com.example.eventorganizer;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import network_structures.EventInfoUpdate;
import network_structures.SectorInfoFixed;

public class SectorLayout extends ItemLayout {

    private final SectorInfoFixed sectorInfoFixed;

    public SectorLayout(SectorInfoFixed sectorInfoFixed) {
        super(R.layout.sector_item);
        this.sectorInfoFixed = sectorInfoFixed;
    }

    @Override
    public void createItemHolder(View view, @Nullable Context context) {
        setItemHolder(new SectorLayoutHolder(view, context));
    }

    @Override
    protected void setItemHolderAttributes() {
        ((SectorLayoutHolder) getItemHolder()).textViewName.setText(sectorInfoFixed.getName());
        ((SectorLayoutHolder) getItemHolder()).textViewAddress.setText(sectorInfoFixed.getAddress());
        String text;
        if (TaskManager.eventInfoUpdate != null) {
            text = "Aktywne atrakcje: " + TaskManager.eventInfoUpdate.getSectors().get(sectorInfoFixed.getId()).getActiveRoomsCount();
        }
        else {
            text = "Aktywne atrakcje: " + sectorInfoFixed.getActiveRooms();
        }
        ((SectorLayoutHolder) getItemHolder()).textViewAvailableRooms.setText(text);
    }

    public void updateItemHolderAttributes(EventInfoUpdate update) {
        String text = "Aktywne atrakcje: " + update.getSectors().get(sectorInfoFixed.getId()).getActiveRoomsCount();
        ((SectorLayoutHolder) getItemHolder()).textViewAvailableRooms.setText(text);
    }

    private class SectorLayoutHolder extends ItemHolder {
        public TextView textViewName;
        public TextView textViewAddress;
        public TextView textViewAvailableRooms;

        public SectorLayoutHolder(View view, Context context) {
            textViewName = view.findViewById(R.id.sector_name);
            textViewAddress = view.findViewById(R.id.sector_address);
            textViewAvailableRooms = view.findViewById(R.id.sector_available_rooms);

            view.findViewById(R.id.sector_field).setOnClickListener(v -> {
                ((HomeActivity) context).setSectorRoomsFragment(sectorInfoFixed.getId());
            });
        }
    }
}
