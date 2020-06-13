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
    public void createItemHolder(View view) {
        setItemHolder(new SectorLayoutHolder(view));
    }

    @Override
    protected void setItemHolderAttributes(@Nullable Context context) {
        ((SectorLayoutHolder) getItemHolder()).textViewName.setText(sectorInfoFixed.getName());
        ((SectorLayoutHolder) getItemHolder()).textViewAddress.setText(sectorInfoFixed.getAddress());
        String text;
        if (GuideAccount.getInstance().getEventInfoUpdate() != null) {
            text = "Aktywne atrakcje: " + GuideAccount.getInstance().getEventInfoUpdate().getSectors().get(sectorInfoFixed.getId()).getActiveRoomsCount();
        }
        else {
            text = "Aktywne atrakcje: " + sectorInfoFixed.getActiveRooms();
        }
        ((SectorLayoutHolder) getItemHolder()).textViewAvailableRooms.setText(text);

        if (context != null) {
            ((SectorLayoutHolder) getItemHolder()).viewSectorField.setOnClickListener(v -> {
                ((HomeActivity) context).setSectorRoomsFragment(sectorInfoFixed.getId());
            });
        }
    }

    public void updateItemHolderAttributes(EventInfoUpdate update) {
        String text = "Aktywne atrakcje: " + update.getSectors().get(sectorInfoFixed.getId()).getActiveRoomsCount();
        ((SectorLayoutHolder) getItemHolder()).textViewAvailableRooms.setText(text);
    }

    private class SectorLayoutHolder extends ItemHolder {
        private final TextView textViewName;
        private final TextView textViewAddress;
        private final TextView textViewAvailableRooms;
        private final View viewSectorField;

        public SectorLayoutHolder(View view) {
            this.textViewName = view.findViewById(R.id.sector_name);
            this.textViewAddress = view.findViewById(R.id.sector_address);
            this.textViewAvailableRooms = view.findViewById(R.id.sector_available_rooms);

            this.viewSectorField = view.findViewById(R.id.sector_field);
        }
    }
}
