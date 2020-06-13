package com.example.eventorganizer;

import network_structures.EventInfoFixed;
import network_structures.EventInfoUpdate;
import network_structures.QueueInfo;

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
    private QueueInfo[] queues;
    private int queuesSize;

    private GuideAccount(EventInfoFixed eventInfoFixed) {
        this.eventInfoFixed = eventInfoFixed;
    }

    public EventInfoFixed getEventInfoFixed() {
        return this.eventInfoFixed;
    }

    public EventInfoUpdate getEventInfoUpdate() {
        return this.eventInfoUpdate;
    }

    public void setEventInfoUpdate(EventInfoUpdate eventInfoUpdate) {
        this.eventInfoUpdate = eventInfoUpdate;
    }

    public QueueInfo[] getQueues() { return this.queues; }

    public void setQueues(QueueInfo[] queues) {
        this.queues = queues;
    }

    public int getQueuesSize() {
        return queuesSize;
    }

    public void setQueuesSize(int queuesSize) { this.queuesSize = queuesSize; }
}
