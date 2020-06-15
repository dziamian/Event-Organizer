package com.example.eventorganizer.list;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.example.eventorganizer.CurrentSession;
import com.example.eventorganizer.HomeActivity;
import com.example.eventorganizer.R;
import network_structures.ReservationInfo;
import network_structures.SectorInfoFixed;

/**
 * Inheriting class contains mutable UI elements related to reservations.
 */
public class ReservationLayout extends ItemLayout {

    /** Reference to object containing reservation information */
    private final ReservationInfo reservationInfo;

    /**
     * Creates basic reservation layout.
     * @param reservationInfo Information about reservation
     */
    public ReservationLayout(ReservationInfo reservationInfo) {
        super(R.layout.reservation_item);
        this.reservationInfo = reservationInfo;
    }

    @Override
    public void createItemHolder(View view) {
        setItemHolder(new ReservationLayoutHolder(view));
    }

    @Override
    protected void setItemHolderAttributes(@Nullable Context context) {
        SectorInfoFixed sectorInfoFixed = CurrentSession.getInstance().getEventInfoFixed().getSectors().get(reservationInfo.getSectorId());
        ((ReservationLayoutHolder) getItemHolder()).textViewReservationRoomName.setText(sectorInfoFixed.getRooms().get(reservationInfo.getRoomId()).getName());
        ((ReservationLayoutHolder) getItemHolder()).textViewReservationSectorName.setText(sectorInfoFixed.getName());
        long secondsLeft = (reservationInfo.getExpirationDate().getTime() - System.currentTimeMillis()) / 1000;
        String text = "Czas do końca rezerwacji: " + secondsLeft + " s";
        ((ReservationLayoutHolder) getItemHolder()).textViewReservationTimer.setText(text);

        if (context != null) {
            ((ReservationLayoutHolder) getItemHolder()).buttonReservationGoRoom.setOnClickListener(v -> {
                ((HomeActivity) context).setRoomFragment(reservationInfo.getSectorId(), reservationInfo.getRoomId());
            });
        }
    }

    /**
     * Updates UI elements with provided information.
     * @param secondsLeft Time before reservation expires in seconds
     */
    public void updateTime(long secondsLeft) {
        String text = "Czas do końca rezerwacji: " + secondsLeft + " s";
        ((ReservationLayoutHolder) getItemHolder()).textViewReservationTimer.setText(text);
    }

    /**
     * Container class for reservation UI elements.
     */
    private class ReservationLayoutHolder extends ItemHolder {
        private final TextView textViewReservationRoomName;
        private final TextView textViewReservationSectorName;
        private final TextView textViewReservationTimer;
        private final Button buttonReservationGoRoom;

        /**
         * Creates basic reservation layout holder. Initialize UI elements.
         * @param view View to search for UI elements
         */
        public ReservationLayoutHolder(View view) {
            this.textViewReservationRoomName = view.findViewById(R.id.reservation_room_name);
            this.textViewReservationSectorName = view.findViewById(R.id.reservation_sector_name);
            this.textViewReservationTimer = view.findViewById(R.id.reservation_timer);

            this.buttonReservationGoRoom = view.findViewById(R.id.reservation_go_room);
        }
    }
}
