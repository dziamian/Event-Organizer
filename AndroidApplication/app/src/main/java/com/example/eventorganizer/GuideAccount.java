package com.example.eventorganizer;

import android.app.Activity;
import network_structures.EventInfoFixed;
import network_structures.EventInfoUpdate;
import network_structures.QueueInfo;
import network_structures.ReservationInfo;

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

    private HomeActivity currentActivity;
    private final EventInfoFixed eventInfoFixed;
    private EventInfoUpdate eventInfoUpdate;
    private QueueInfo[] queues;
    private int queuesSize = 0;
    private ReservationInfo reservationInfo;

    private GuideAccount(EventInfoFixed eventInfoFixed) {
        this.eventInfoFixed = eventInfoFixed;
    }

    public HomeActivity getCurrentActivity() {
        return this.currentActivity;
    }

    public void setCurrentActivity(HomeActivity activity) {
        this.currentActivity = activity;
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

    public ReservationInfo getReservationInfo() {
        return reservationInfo;
    }

    public void setReservationInfo(ReservationInfo reservationInfo) {
        this.reservationInfo = reservationInfo;
    }
}
