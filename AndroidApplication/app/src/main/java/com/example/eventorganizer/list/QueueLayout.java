package com.example.eventorganizer.list;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.example.eventorganizer.CurrentSession;
import com.example.eventorganizer.HomeActivity;
import com.example.eventorganizer.MainActivity;
import com.example.eventorganizer.R;
import network_structures.BaseMessage;
import network_structures.QueueInfo;
import network_structures.SectorInfoFixed;

/**
 * Inheriting class contains mutable UI elements related to queues.
 */
public class QueueLayout extends ItemLayout {

    /** Reference to object containing queue information */
    private final QueueInfo queueInfo;
    /** Message to send to {@link com.example.eventorganizer.TaskManager} if Leave Queue button was pressed */
    private BaseMessage leavingQueueMessage;

    /**
     * Creates basic queue layout.
     * @param queueInfo Information about queue
     */
    public QueueLayout(QueueInfo queueInfo) {
        super(R.layout.queue_item);
        this.queueInfo = queueInfo;
    }

    /**
     * Assigns new {@link BaseMessage} instance to be used by {@link com.example.eventorganizer.TaskManager}
     * @param leavingQueueMessage Message to send to {@link com.example.eventorganizer.TaskManager}
     */
    public void setLeavingQueueMessage(BaseMessage leavingQueueMessage) {
        this.leavingQueueMessage = leavingQueueMessage;
    }

    @Override
    public void createItemHolder(View view) {
        setItemHolder(new QueueLayoutHolder(view));
    }

    @Override
    protected void setItemHolderAttributes(@Nullable Context context) {
        SectorInfoFixed sectorInfoFixed = CurrentSession.getInstance().getEventInfoFixed().getSectors().get(queueInfo.getSectorId());
        ((QueueLayoutHolder) getItemHolder()).textViewQueueRoomName.setText(sectorInfoFixed.getRooms().get(queueInfo.getRoomId()).getName());
        ((QueueLayoutHolder) getItemHolder()).textViewQueueSectorName.setText(sectorInfoFixed.getName());
        String text = "Pozycja w kolejce: " + queueInfo.getPositionInQueue();
        ((QueueLayoutHolder) getItemHolder()).textViewQueuePosition.setText(text);

        ((QueueLayoutHolder) getItemHolder()).buttonQueueQuit.setOnClickListener(v -> {
            MainActivity.taskManager.addIncomingMessage(leavingQueueMessage);
        });
        if (context != null) {
            ((QueueLayoutHolder) getItemHolder()).buttonQueueGoRoom.setOnClickListener(v -> {
                ((HomeActivity) context).setRoomFragment(queueInfo.getSectorId(), queueInfo.getRoomId());
            });
        }
    }

    /**
     * Container class for queue UI elements.
     */
    private class QueueLayoutHolder extends ItemHolder {
        private final TextView textViewQueueRoomName;
        private final TextView textViewQueueSectorName;
        private final TextView textViewQueuePosition;
        private final Button buttonQueueQuit;
        private final Button buttonQueueGoRoom;

        /**
         * Creates basic queue layout holder. Initialize UI elements.
         * @param view View to search for UI elements
         */
        public QueueLayoutHolder(View view) {
            this.textViewQueueRoomName = view.findViewById(R.id.queue_room_name);
            this.textViewQueueSectorName = view.findViewById(R.id.queue_sector_name);
            this.textViewQueuePosition = view.findViewById(R.id.queue_position);

            this.buttonQueueQuit = view.findViewById(R.id.queue_quit);
            this.buttonQueueGoRoom = view.findViewById(R.id.queue_go_room);
        }
    }
}
