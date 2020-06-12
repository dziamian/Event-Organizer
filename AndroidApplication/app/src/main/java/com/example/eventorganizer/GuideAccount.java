package com.example.eventorganizer;

import network_structures.EventInfoFixed;
import network_structures.EventInfoUpdate;
import network_structures.QueueInfo;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public class GuideAccount {

    private static GuideAccount instance = null;

    public static GuideAccount getInstance() {
        return instance;
    }

    public static boolean createInstance(EventInfoFixed eventInfoFixed) {
        if (instance == null) {
            GuideAccount.instance = new GuideAccount(eventInfoFixed);
            return true;
        }
        return false;
    }

    private final EventInfoFixed eventInfoFixed;
    private EventInfoUpdate eventInfoUpdate;
    private ArrayList<QueueInfo> queues;

    private GuideAccount(EventInfoFixed eventInfoFixed) {
        this.eventInfoFixed = eventInfoFixed;
    }

    public EventInfoFixed getEventInfoFixed() {
        return eventInfoFixed;
    }

    public EventInfoUpdate getEventInfoUpdate() {
        return eventInfoUpdate;
    }

    public void setEventInfoUpdate(EventInfoUpdate eventInfoUpdate) {
        this.eventInfoUpdate = eventInfoUpdate;
    }

    public QueueInfo getQueue(int position) {
        return queues.get(position);
    }

    public void addNewQueue(ObjectId sectorId, ObjectId roomId, int positionInQueue) {
        queues.add(new QueueInfo(sectorId, roomId, positionInQueue));
    }
}
