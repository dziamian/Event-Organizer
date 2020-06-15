package com.example.eventorganizer;

import network_structures.EventInfoFixed;
import network_structures.EventInfoUpdate;
import network_structures.QueueInfo;
import network_structures.ReservationInfo;

/**
 * Class containing all useful information about established session with server.
 */
public class CurrentSession {

    /** A static reference to {@link CurrentSession} object */
    private static CurrentSession instance = null;

    /**
     * Returns static reference of {@link CurrentSession} instance.
     * @return Static reference of {@link CurrentSession} instance
     */
    public static CurrentSession getInstance() {
        return CurrentSession.instance;
    }

    /**
     * Creates single instance of {@link CurrentSession}
     * @param eventInfoFixed Initial information about event needed to create an instance {@link EventInfoFixed}
     * @return True if instance has been created and false if instance was created earlier
     */
    public static boolean createInstance(EventInfoFixed eventInfoFixed) {
        if (instance == null) {
            CurrentSession.instance = new CurrentSession(eventInfoFixed);
            return true;
        }
        return false;
    }

    /** Reference to created instance of {@link HomeActivity} */
    private HomeActivity homeActivity;
    /** Reference to instance of {@link EventInfoFixed} received from server */
    private final EventInfoFixed eventInfoFixed;
    /** Reference to instance of {@link EventInfoUpdate} which can be modified by messages from server */
    private EventInfoUpdate eventInfoUpdate;
    /** Reference to array of {@link QueueInfo} instances which can be modified by messages from server */
    private QueueInfo[] queues;
    /** Variable containing number of queues modified by incoming messages from server */
    private int numberOfQueues = 0;
    /** Reference to instance of {@link ReservationInfo} which can be modified by messages from server */
    private ReservationInfo reservationInfo;

    /**
     * A private Constructor of {@link CurrentSession} that prevents any other class from instantiating.
     * @param eventInfoFixed Initial information about event {@link EventInfoFixed}
     */
    private CurrentSession(EventInfoFixed eventInfoFixed) {
        this.eventInfoFixed = eventInfoFixed;
    }

    /**
     * Returns instance of {@link HomeActivity}.
     * @return Created instance of {@link HomeActivity}
     */
    public HomeActivity getHomeActivity() {
        return this.homeActivity;
    }

    /**
     * Assigns new reference of {@link HomeActivity} instance.
     * @param activity Reference to instance of {@link HomeActivity}
     */
    public void setHomeActivity(HomeActivity activity) {
        this.homeActivity = activity;
    }

    /**
     * Returns instance of {@link EventInfoFixed}.
     * @return Instance of {@link EventInfoFixed}
     */
    public EventInfoFixed getEventInfoFixed() {
        return this.eventInfoFixed;
    }

    /**
     * Returns instance of {@link EventInfoUpdate}.
     * @return Instance of {@link EventInfoUpdate}
     */
    public EventInfoUpdate getEventInfoUpdate() {
        return this.eventInfoUpdate;
    }

    /**
     * Assigns new reference of {@link EventInfoUpdate} instance.
     * @param eventInfoUpdate Reference to instance of {@link EventInfoUpdate}
     */
    public void setEventInfoUpdate(EventInfoUpdate eventInfoUpdate) {
        this.eventInfoUpdate = eventInfoUpdate;
    }

    /**
     * Returns array containing {@link QueueInfo}.
     * @return Array of {@link QueueInfo}
     */
    public QueueInfo[] getQueues() { return this.queues; }

    /**
     * Assigns new reference of {@link QueueInfo} array.
     * @param queues Reference of {@link QueueInfo} array.
     */
    public void setQueues(QueueInfo[] queues) {
        this.queues = queues;
    }

    /**
     * Returns number of queues in current session.
     * @return Given number of queues
     */
    public int getNumberOfQueues() {
        return numberOfQueues;
    }

    /**
     * Assigns new value to <b>numberOfQueues</b>.
     * @param numberOfQueues Given number of queues
     */
    public void setNumberOfQueues(int numberOfQueues) { this.numberOfQueues = numberOfQueues; }

    /**
     * Returns instance of {@link ReservationInfo}.
     * @return Instance of {@link ReservationInfo}
     */
    public ReservationInfo getReservationInfo() {
        return reservationInfo;
    }

    /**
     * Assigns new reference of {@link ReservationInfo}.
     * @param reservationInfo Given reference of {@link ReservationInfo}
     */
    public void setReservationInfo(ReservationInfo reservationInfo) {
        this.reservationInfo = reservationInfo;
    }
}
