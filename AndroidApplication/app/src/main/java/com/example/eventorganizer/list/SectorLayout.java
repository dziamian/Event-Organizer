package com.example.eventorganizer.list;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.example.eventorganizer.CurrentSession;
import com.example.eventorganizer.HomeActivity;
import com.example.eventorganizer.R;
import network_structures.EventInfoUpdate;
import network_structures.SectorInfoFixed;

/**
 * Inheriting class contains mutable UI elements related to sectors.
 */
public class SectorLayout extends ItemLayout {

    /** Reference to object containing sector information */
    private final SectorInfoFixed sectorInfoFixed;

    /**
     * Creates basic sector layout.
     * @param sectorInfoFixed Information about sector
     */
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
        if (CurrentSession.getInstance().getEventInfoUpdate() != null) {
            text = "Aktywne atrakcje: " + CurrentSession.getInstance().getEventInfoUpdate().getSectors().get(sectorInfoFixed.getId()).getActiveRoomsCount();
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

    /**
     * Updates UI elements with provided information.
     * @param update Information about event
     */
    public void updateItemHolderAttributes(EventInfoUpdate update) {
        String text = "Aktywne atrakcje: " + update.getSectors().get(sectorInfoFixed.getId()).getActiveRoomsCount();
        ((SectorLayoutHolder) getItemHolder()).textViewAvailableRooms.setText(text);
    }

    /**
     * Container class for sector UI elements.
     */
    private class SectorLayoutHolder extends ItemHolder {
        private final TextView textViewName;
        private final TextView textViewAddress;
        private final TextView textViewAvailableRooms;
        private final View viewSectorField;

        /**
         * Creates basic sector layout holder. Initialize UI elements.
         * @param view View to search for UI elements
         */
        public SectorLayoutHolder(View view) {
            this.textViewName = view.findViewById(R.id.sector_name);
            this.textViewAddress = view.findViewById(R.id.sector_address);
            this.textViewAvailableRooms = view.findViewById(R.id.sector_available_rooms);

            this.viewSectorField = view.findViewById(R.id.sector_field);
        }
    }
}
