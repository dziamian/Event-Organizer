package com.example.eventorganizer;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import network_structures.RoomInfo;

public class RoomLayout extends ItemLayout {

    private final RoomInfo roomInfo;
    private RoomLayoutHolder roomLayoutHolder;

    public RoomLayout(RoomInfo roomInfo) {
        super(R.layout.room_item);
        this.roomInfo = roomInfo;
    }

    @Override
    public void createItemHolder(View view) {

    }

    @Override
    protected Object getItemHolder() {
        return roomLayoutHolder;
    }

    @Override
    public void setLayout(View view, Context context) {
        roomLayoutHolder.textViewName = view.findViewById(R.id.room_name);
        roomLayoutHolder.textViewLocation = view.findViewById(R.id.room_location);
        roomLayoutHolder.textViewStateDetails = view.findViewById(R.id.room_state_details);
        roomLayoutHolder.textViewState = view.findViewById(R.id.room_state);

        roomLayoutHolder.textViewName.setText(roomInfo.name);
        roomLayoutHolder.textViewLocation.setText(roomInfo.location);
        roomLayoutHolder.textViewStateDetails.setText("Grup w kolejce: " + 0);
        roomLayoutHolder.textViewState.setText("Dostępny!");

        view.findViewById(R.id.room_field).setOnClickListener(v -> {

        });
    }

    @Override
    public void setItemHolder(Object itemHolder) {
        if (itemHolder instanceof RoomLayoutHolder) {
            roomLayoutHolder = (RoomLayoutHolder) itemHolder;
        }
    }

    private static class RoomLayoutHolder extends ItemHolder {
        TextView textViewName, textViewLocation, textViewStateDetails, textViewState;
    }
}
