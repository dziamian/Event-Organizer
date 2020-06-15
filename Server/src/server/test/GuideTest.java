package server.test;

import network_structures.EventInfoUpdate;
import network_structures.NetworkMessage;
import network_structures.RoomInfoUpdate;
import network_structures.SectorInfoUpdate;
import org.bson.types.ObjectId;
import queue.Room;
import queue.TourGroup;
import server.Guide;
import server.Server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

class GuideTest {
    private final ConcurrentLinkedQueue<Server.Task> taskQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedDeque<NetworkMessage> sentMessagesQueue = new ConcurrentLinkedDeque<>();
    private final Supplier<EventInfoUpdate> updateSupplier = new Supplier<EventInfoUpdate>() {
        private final EventInfoUpdate info;

        {
            info = new EventInfoUpdate();
            ObjectId soid = new ObjectId("616161616161616161616161");
            info.getSectors().put(
                    soid,
                    new SectorInfoUpdate(soid)
            );
            info.getSectors().values().forEach((siu) -> {
                ObjectId roid = new ObjectId("616161616161616161616162");
                RoomInfoUpdate riu = new RoomInfoUpdate(roid);
                siu.getRooms().put(roid, riu);
                riu.setQueueSize(0);
                riu.setState(Room.State.RESERVED.toString());
            });
        }

        @Override
        public EventInfoUpdate get() {
            return info;
        }
    };
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    private Guide guide = null;

    GuideTest() {
        try {
            out = new ObjectOutputStream() {
                @Override
                protected void writeObjectOverride(Object obj) throws IOException {
                    sentMessagesQueue.offer((NetworkMessage)obj);
                }
            };
            in = new ObjectInputStream() {
                private final String[] commands = new String[] {
                        "add_to_queue",
                        "remove_from_queue",
                        "view_tickets",
                        "Hello World!"
                };

                private int i = 0;

                @Override
                protected Object readObjectOverride() throws IOException, ClassNotFoundException {
                    if (i < 0)
                        i = 0;
                    return new NetworkMessage(
                            commands[i % 4],
                            new String[0],
                            null,
                            i
                    );
                }
            };

        } catch (IOException ex) {
            System.err.println("Test initialization failed");
            System.err.println(ex.getMessage());
        }
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        TourGroup group = new TourGroup();
        guide = new Guide(
                out,
                in,
                group,
                taskQueue::add,
                (command) -> true,
                updateSupplier
        );
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        guide.dismiss();
        taskQueue.clear();
        sentMessagesQueue.clear();
    }

    @org.junit.jupiter.api.Test
    void stopOutputThread() {
        Thread t = new Thread(guide::handlingOutput);
        t.start();
        while (!t.isAlive());
        NetworkMessage message = new NetworkMessage(
                "test",
                null,
                null,
                0
        );
        guide.stopOutputThread();
        try {
            t.join();
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
        guide.addOutgoingMessage(message);
        assert sentMessagesQueue.peekLast() != message : "Output thread has not stopped working";
    }

    @org.junit.jupiter.api.Test
    void changeGroup() {
        TourGroup oldGroup = guide.getGroup();
        TourGroup newGroup = new TourGroup();
        assert guide.changeGroup(newGroup) : "The guide could not be moved while it should have been";
        assert newGroup.hasThisGuide(guide) : "This guide is not a part of his new group";
        assert !oldGroup.hasThisGuide(guide) : "This guide is still a part of his old group";
    }


    @org.junit.jupiter.api.Test
    void handlingInput() {
        new Thread(
                () -> {
                    try {
                        guide.handlingInput();
                    } catch (Exception ex) {
                        System.err.println(ex.getMessage());
                    }
                }
        ).start();
        while (taskQueue.size() < 8);
        Object comparator = new Object() {
            public boolean equals(Server.Task o) {
                if (o == null || o.getCommand() == null)
                    return false;
                return "update".equals(o.getCommand());
            }

            @Override
            public boolean equals(Object o) {
                if (o instanceof Server.Task)
                    return equals((Server.Task)o);
                return o == this;
            }
        };
        guide.dismiss();
        assert !taskQueue.contains(comparator) : "Update request has been forwarded to server";
    }

    @org.junit.jupiter.api.Test
    void handlingOutput() {
        new Thread(guide::handlingOutput).start();
        ConcurrentLinkedQueue<NetworkMessage> messagesQueued = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < 8; ++i)
            messagesQueued.offer(new NetworkMessage(
                "test",
                    null,
                    null,
                    0
            ));
        for (NetworkMessage nm : messagesQueued) {
            guide.addOutgoingMessage(nm);
        }

        Iterator<NetworkMessage> itMessagesQueued = messagesQueued.iterator();
        Iterator<NetworkMessage> itMessagesSent = sentMessagesQueue.iterator();
        while (itMessagesSent.hasNext()) {
            assert itMessagesSent.next() == itMessagesQueued.next() : "Messages are not being sent in correct order";
        }
        guide.dismiss();
    }

    @org.junit.jupiter.api.Test
    void removeFromSystem() {
        assert guide.getGroup().hasThisGuide(guide) : "The group has not been assigned correctly";
        guide.removeFromSystem();
        assert !guide.getGroup().hasThisGuide(guide) : "The group has not been removed correctly";
    }
}
