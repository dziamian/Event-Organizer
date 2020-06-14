package server.test;

import network_structures.EventInfoUpdate;
import network_structures.RoomInfoUpdate;
import network_structures.SectorInfoUpdate;
import org.bson.types.ObjectId;
import queue.Room;
import queue.TourGroup;
import server.Guide;
import server.Server;
import server.Server.Task;

import java.io.*;
import java.util.LinkedList;
import java.util.function.Supplier;

// import static org.junit.jupiter.api.Assertions.*;

class GuideTest {
    private static final String outputFileName = "guidetestoutput.txt";
    private static final String inputFileName = "guidetestinput.txt";
    File outputFile = new File(outputFileName);
    File inputFile = new File(inputFileName);
    LinkedList<Server.Task> taskQueue = new LinkedList<>();
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    TourGroup group = null;
    Guide guide = null;

    GuideTest() {
        try {
            outputFile.createNewFile();
            out = new ObjectOutputStream(new FileOutputStream(inputFile));
            in = new ObjectInputStream(new FileInputStream(inputFile));
            group = new TourGroup();
            guide = new Guide(
                    out,
                    in,
                    group,
                    (task) -> taskQueue.add(task),
                    (command) -> true,
                    new Supplier<EventInfoUpdate>() {
                        private final EventInfoUpdate info;

                        {
                            info = new EventInfoUpdate();
                            ObjectId soid = new ObjectId("sec-01");
                            info.getSectors().put(
                                soid,
                                new SectorInfoUpdate(soid)
                            );
                            info.getSectors().values().forEach((siu) -> {
                                ObjectId roid = new ObjectId("room-01");
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
                    }
            );
        } catch (IOException ex) {
            System.err.println("Test initialization failed");
            System.err.println(ex.getMessage());
        }
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void stopOutputThread() {

    }

    @org.junit.jupiter.api.Test
    void changeGroup() {
    }

    @org.junit.jupiter.api.Test
    void handlingInput() {
    }

    @org.junit.jupiter.api.Test
    void handlingOutput() {
    }

    @org.junit.jupiter.api.Test
    void removeFromSystem() {
    }
}