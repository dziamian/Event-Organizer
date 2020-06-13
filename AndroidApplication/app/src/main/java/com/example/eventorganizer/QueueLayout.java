package com.example.eventorganizer;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import network_structures.BaseMessage;
import network_structures.QueueInfo;
import network_structures.SectorInfoFixed;

public class QueueLayout extends ItemLayout {

    private final QueueInfo queueInfo;
    private BaseMessage leavingQueueMessage;

    protected QueueLayout(QueueInfo queueInfo) {
        super(R.layout.queue_item);
        this.queueInfo = queueInfo;
    }

    public void setLeavingQueueMessage(BaseMessage leavingQueueMessage) {
        this.leavingQueueMessage = leavingQueueMessage;
    }

    @Override
    public void createItemHolder(View view) {
        setItemHolder(new QueueLayoutHolder(view));
    }

    @Override
    protected void setItemHolderAttributes(@Nullable Context context) {
        SectorInfoFixed sectorInfoFixed = GuideAccount.getInstance().getEventInfoFixed().getSectors().get(queueInfo.getSectorId());
        ((QueueLayoutHolder) getItemHolder()).textViewQueueRoomName.setText(sectorInfoFixed.getRooms().get(queueInfo.getRoomId()).getName());
        ((QueueLayoutHolder) getItemHolder()).textViewQueueSectorName.setText(sectorInfoFixed.getName());
        String text = "Pozycja w kolejce: " + queueInfo.getPositionInQueue();
        ((QueueLayoutHolder) getItemHolder()).textViewQueuePosition.setText(text);

        ((QueueLayoutHolder) getItemHolder()).buttonQueueQuit.setOnClickListener(v -> {
            MainActivity.taskManager.addIncomingMessage(leavingQueueMessage);
        });
        if (context != null) {
            ((QueueLayoutHolder) getItemHolder()).buttonQueueGoRoom.setOnClickListener(v -> {
                ((HomeActivity) context).setRoomActivity(queueInfo.getSectorId(), queueInfo.getRoomId());
            });
        }
    }

    private class QueueLayoutHolder extends ItemHolder {
        private final TextView textViewQueueRoomName;
        private final TextView textViewQueueSectorName;
        private final TextView textViewQueuePosition;
        private final Button buttonQueueQuit;
        private final Button buttonQueueGoRoom;

        public QueueLayoutHolder(View view) {
            this.textViewQueueRoomName = view.findViewById(R.id.queue_room_name);
            this.textViewQueueSectorName = view.findViewById(R.id.queue_sector_name);
            this.textViewQueuePosition = view.findViewById(R.id.queue_position);

            this.buttonQueueQuit = view.findViewById(R.id.queue_quit);
            this.buttonQueueGoRoom = view.findViewById(R.id.queue_go_room);
        }
    }
}
