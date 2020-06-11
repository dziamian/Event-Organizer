package com.example.eventorganizer;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import network_structures.EventInfoUpdate;
import network_structures.SectorInfoFixed;
import network_structures.SectorInfoUpdate;

public class SectorLayout extends ItemLayout {

    private SectorInfoFixed sectorInfoFixed;

    public SectorLayout(SectorInfoFixed sectorInfoFixed) {
        super(R.layout.sector_item);
        this.sectorInfoFixed = sectorInfoFixed;
    }

    @Override
    public void createItemHolder(View view) {
        setItemHolder(new SectorLayoutHolder(view));
    }

    @Override
    protected void setItemHolderAttributes() {
        ((SectorLayoutHolder) getItemHolder()).textViewName.setText(sectorInfoFixed.getName());
        ((SectorLayoutHolder) getItemHolder()).textViewAddress.setText(sectorInfoFixed.getAddress());
        ((SectorLayoutHolder) getItemHolder()).textViewAvailableRooms.setText(String.valueOf(sectorInfoFixed.getRooms().size()));
    }

    public void updateItemHolderAttributes(EventInfoUpdate update) {
        ((SectorLayoutHolder) getItemHolder()).textViewAvailableRooms.setText(String.valueOf(update.getSectors().get(sectorInfoFixed.getId()).getActiveRooms()));
    }

    private class SectorLayoutHolder extends ItemHolder {
        public TextView textViewName;
        public TextView textViewAddress;
        public TextView textViewAvailableRooms;

        public SectorLayoutHolder(View view) {
            textViewName = view.findViewById(R.id.sector_name);
            textViewAddress = view.findViewById(R.id.sector_address);
            textViewAvailableRooms = view.findViewById(R.id.sector_available_rooms);
        }
    }
    /*private final SectorInfoFixed sectorInfoFixed;
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
    }*/
}
