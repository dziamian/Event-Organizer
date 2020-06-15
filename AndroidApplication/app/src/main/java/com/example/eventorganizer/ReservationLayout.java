package com.example.eventorganizer;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import network_structures.ReservationInfo;
import network_structures.SectorInfoFixed;

public class ReservationLayout extends ItemLayout {

    private final ReservationInfo reservationInfo;

    protected ReservationLayout(ReservationInfo reservationInfo) {
        super(R.layout.reservation_item);
        this.reservationInfo = reservationInfo;
    }

    @Override
    public void createItemHolder(View view) {
        setItemHolder(new ReservationLayoutHolder(view));
    }

    @Override
    protected void setItemHolderAttributes(@Nullable Context context) {
        SectorInfoFixed sectorInfoFixed = GuideAccount.getInstance().getEventInfoFixed().getSectors().get(reservationInfo.getSectorId());
        ((ReservationLayoutHolder) getItemHolder()).textViewReservationRoomName.setText(sectorInfoFixed.getRooms().get(reservationInfo.getRoomId()).getName());
        ((ReservationLayoutHolder) getItemHolder()).textViewReservationSectorName.setText(sectorInfoFixed.getName());
        long secondsLeft = (reservationInfo.getExpirationDate().getTime() - System.currentTimeMillis()) / 1000;
        String text = "Czas do końca rezerwacji: " + secondsLeft + " s";
        ((ReservationLayoutHolder) getItemHolder()).textViewReservationTimer.setText(text);

        if (context != null) {
            ((ReservationLayoutHolder) getItemHolder()).buttonReservationGoRoom.setOnClickListener(v -> {
                ((HomeActivity) context).setRoomActivity(reservationInfo.getSectorId(), reservationInfo.getRoomId());
            });
        }
    }

    public void updateTime(long secondsLeft) {
        String text = "Czas do końca rezerwacji: " + secondsLeft + " s";
        ((ReservationLayoutHolder) getItemHolder()).textViewReservationTimer.setText(text);
    }

    private class ReservationLayoutHolder extends ItemHolder {
        private final TextView textViewReservationRoomName;
        private final TextView textViewReservationSectorName;
        private final TextView textViewReservationTimer;
        private final Button buttonReservationGoRoom;

        public ReservationLayoutHolder(View view) {
            this.textViewReservationRoomName = view.findViewById(R.id.reservation_room_name);
            this.textViewReservationSectorName = view.findViewById(R.id.reservation_sector_name);
            this.textViewReservationTimer = view.findViewById(R.id.reservation_timer);

            this.buttonReservationGoRoom = view.findViewById(R.id.reservation_go_room);
        }
    }
}
