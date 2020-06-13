package com.example.eventorganizer;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import network_structures.EventInfoUpdate;
import network_structures.RoomInfoFixed;
import org.bson.types.ObjectId;

public class SectorRoomLayout extends ItemLayout {

    private final RoomInfoFixed roomInfoFixed;

    public SectorRoomLayout(RoomInfoFixed roomInfoFixed) {
        super(R.layout.room_item);
        this.roomInfoFixed = roomInfoFixed;
    }

    @Override
    public void createItemHolder(View view, @Nullable Context context) {
        setItemHolder(new RoomLayoutHolder(view, context));
    }

    @Override
    protected void setItemHolderAttributes() {
        ((RoomLayoutHolder) getItemHolder()).textViewName.setText(roomInfoFixed.getName());
        ((RoomLayoutHolder) getItemHolder()).textViewLocation.setText(roomInfoFixed.getLocation());
        String stateDetails;
        if (GuideAccount.getInstance().getEventInfoUpdate() != null) {
            ((RoomLayoutHolder) getItemHolder()).textViewRoomState.setText(GuideAccount.getInstance().getEventInfoUpdate().getSectors().get(roomInfoFixed.getSectorId()).getRooms().get(roomInfoFixed.getId()).getState());
            stateDetails = "Grup w kolejce: " + GuideAccount.getInstance().getEventInfoUpdate().getSectors().get(roomInfoFixed.getSectorId()).getRooms().get(roomInfoFixed.getId()).getQueueSize();
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
        private final TextView textViewName;
        private final TextView textViewLocation;
        private final TextView textViewRoomState;
        private final TextView textViewRoomStateDetails;

        public RoomLayoutHolder(View view, Context context) {
            textViewName = view.findViewById(R.id.room_name);
            textViewLocation = view.findViewById(R.id.room_location);
            textViewRoomState = view.findViewById(R.id.room_state);
            textViewRoomStateDetails = view.findViewById(R.id.room_state_details);

            view.findViewById(R.id.room_field).setOnClickListener(v -> {
                ((HomeActivity) context).setRoomActivity(roomInfoFixed.getSectorId(), roomInfoFixed.getId());
            });
        }
    }
}
