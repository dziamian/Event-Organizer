package com.example.eventorganizer;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import network_structures.EventInfoUpdate;
import network_structures.RoomInfoFixed;
import org.bson.types.ObjectId;

public class RoomLayout extends ItemLayout {

    private final RoomInfoFixed roomInfoFixed;

    public RoomLayout(RoomInfoFixed roomInfoFixed) {
        super(R.layout.room_item);
        this.roomInfoFixed = roomInfoFixed;
    }

    @Override
    public void createItemHolder(View view, @Nullable Context context) {
        setItemHolder(new RoomLayoutHolder(view));
    }

    @Override
    protected void setItemHolderAttributes() {
        ((RoomLayoutHolder) getItemHolder()).textViewName.setText(roomInfoFixed.getName());
        ((RoomLayoutHolder) getItemHolder()).textViewLocation.setText(roomInfoFixed.getLocation());
        String stateDetails;
        if (TaskManager.eventInfoUpdate != null) {
            ((RoomLayoutHolder) getItemHolder()).textViewRoomState.setText(TaskManager.eventInfoUpdate.getSectors().get(roomInfoFixed.getSectorId()).getRooms().get(roomInfoFixed.getId()).getState());
            stateDetails = "Grup w kolejce: " + TaskManager.eventInfoUpdate.getSectors().get(roomInfoFixed.getSectorId()).getRooms().get(roomInfoFixed.getId()).getQueueSize();
            ((RoomLayoutHolder) getItemHolder()).textViewRoomStateDetails.setText(stateDetails);
        } else {
            ((RoomLayoutHolder) getItemHolder()).textViewRoomState.setText(roomInfoFixed.getState());
            stateDetails = "Grup w kolejce: " + roomInfoFixed.getQueueSize();
            ((RoomLayoutHolder) getItemHolder()).textViewRoomStateDetails.setText(stateDetails);
        }
    }

    public void updateItemHolderAttributes(EventInfoUpdate update, ObjectId sectorId) {
        ((RoomLayoutHolder) getItemHolder()).textViewRoomState.setText(update.getSectors().get(sectorId).getRooms().get(roomInfoFixed.getId()).getState());
        String stateDetails = "Grup w kolejce: " + update.getSectors().get(sectorId).getRooms().get(roomInfoFixed.getId()).getQueueSize();
        ((RoomLayoutHolder) getItemHolder()).textViewRoomStateDetails.setText(stateDetails);
    }

    private class RoomLayoutHolder extends ItemHolder {
        public TextView textViewName;
        public TextView textViewLocation;
        public TextView textViewRoomState;
        public TextView textViewRoomStateDetails;

        public RoomLayoutHolder(View view) {
            textViewName = view.findViewById(R.id.room_name);
            textViewLocation = view.findViewById(R.id.room_location);
            textViewRoomState = view.findViewById(R.id.room_state);
            textViewRoomStateDetails = view.findViewById(R.id.room_state_details);

            //view.findViewById(...)
        }
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
