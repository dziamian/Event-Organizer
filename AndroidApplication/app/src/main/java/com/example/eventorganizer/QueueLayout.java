package com.example.eventorganizer;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import network_structures.QueueInfo;
import network_structures.SectorInfoFixed;

public class QueueLayout extends ItemLayout {

    private final QueueInfo queueInfo;

    protected QueueLayout(QueueInfo queueInfo) {
        super(R.layout.room_item);
        this.queueInfo = queueInfo;
    }

    @Override
    public void createItemHolder(View view, @Nullable Context context) {
        setItemHolder(new QueueLayoutHolder(view, context));
    }

    @Override
    protected void setItemHolderAttributes() {
        SectorInfoFixed sectorInfoFixed = GuideAccount.getInstance().getEventInfoFixed().getSectors().get(queueInfo.getSectorId());
        ((QueueLayoutHolder) getItemHolder()).textViewQueueRoomName.setText(sectorInfoFixed.getRooms().get(queueInfo.getRoomId()).getName());
        ((QueueLayoutHolder) getItemHolder()).textViewQueueSectorName.setText(sectorInfoFixed.getName());
        String text = "Pozycja w kolejce: " + queueInfo.getPositionInQueue();
        ((QueueLayoutHolder) getItemHolder()).textViewQueuePosition.setText(text);
    }

    private class QueueLayoutHolder extends ItemHolder {
        private final TextView textViewQueueRoomName;
        private final TextView textViewQueueSectorName;
        private final TextView textViewQueuePosition;

        public QueueLayoutHolder(View view, Context context) {
            this.textViewQueueRoomName = view.findViewById(R.id.queue_room_name);
            this.textViewQueueSectorName = view.findViewById(R.id.queue_sector_name);
            this.textViewQueuePosition = view.findViewById(R.id.queue_position);

            view.findViewById(R.id.queue_quit).setOnClickListener(v -> {

            });
            view.findViewById(R.id.queue_go_room).setOnClickListener(v -> {

            });
        }
    }
}
