package com.example.eventorganizer;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import network_structures.RoomInfoFixed;

public class RoomLayout extends ItemLayout {

    public RoomLayout(int resId) {
        super(resId);
    }

    @Override
    public void createItemHolder(View view, @Nullable Context context) {

    }

    @Override
    protected void setItemHolderAttributes() {

    }

    /*private final RoomInfoFixed roomInfoFixed;
    private RoomLayoutHolder roomLayoutHolder;

    public RoomLayout(RoomInfoFixed roomInfoFixed) {
        super(R.layout.room_item);
        this.roomInfoFixed = roomInfoFixed;
        this.roomLayoutHolder = new RoomLayoutHolder();
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

        roomLayoutHolder.textViewName.setText(roomInfoFixed.getName());
        roomLayoutHolder.textViewLocation.setText(roomInfoFixed.getLocation());
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
    }*/
}
