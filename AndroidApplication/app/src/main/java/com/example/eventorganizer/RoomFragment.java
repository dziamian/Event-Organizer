package com.example.eventorganizer;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import network_structures.BaseMessage;
import network_structures.RoomInfoFixed;
import network_structures.SectorInfoFixed;
import org.bson.types.ObjectId;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RoomFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_SECTOR_ID = "sectorID";
    private static final String ARG_ROOM_ID = "roomID";

    // TODO: Rename and change types of parameters
    private String sectorId;
    private String roomId;

    public RoomFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RoomFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RoomFragment newInstance(String sectorId, String roomId) {
        RoomFragment fragment = new RoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SECTOR_ID, sectorId);
        args.putString(ARG_ROOM_ID, roomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.sectorId = getArguments().getString(ARG_SECTOR_ID);
            this.roomId = getArguments().getString(ARG_ROOM_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeActivity.setUpdating(false);
        HomeActivity.setShowingTickets(false);

        ObjectId sectorId = new ObjectId(this.sectorId);
        ObjectId roomId = new ObjectId(this.roomId);
        SectorInfoFixed sectorInfoFixed = GuideAccount.getInstance().getEventInfoFixed().getSectors().get(sectorId);
        RoomInfoFixed roomInfoFixed = sectorInfoFixed.getRooms().get(roomId);

        ((HomeActivity) Objects.requireNonNull(getActivity())).getRooms().setVisible(true);
        ((HomeActivity) getActivity()).getRooms().setTitle(sectorInfoFixed.getName() + " - " + roomInfoFixed.getName());
        getActivity().setTitle(sectorInfoFixed.getName() + " - " + roomInfoFixed.getName());
        ((HomeActivity) getActivity()).getNavigationView().setCheckedItem(R.id.nav_rooms);
        ((HomeActivity) getActivity()).setSelectedItemId(R.id.nav_rooms);

        View rootView = inflater.inflate(R.layout.fragment_room, container, false);

        ((TextView)rootView.findViewById(R.id.room_name)).setText(roomInfoFixed.getName());
        ((TextView)rootView.findViewById(R.id.room_location)).setText(roomInfoFixed.getLocation());
        ((TextView)rootView.findViewById(R.id.room_description)).setText(roomInfoFixed.getDescription());

        rootView.findViewById(R.id.room_add_to_queue).setOnClickListener(v -> {
            MainActivity.taskManager.addIncomingMessage(new BaseMessage(
                    "add_to_queue",
                    new String[] { this.sectorId, this.roomId },
                    new Runnable[] { () -> { //prawidlowe dodanie do kolejki
                        getActivity().runOnUiThread(() -> {
                            int numberOfQueues = GuideAccount.getInstance().getQueuesSize();
                            ((HomeActivity) getActivity()).setQueueBadgeText(GuideAccount.getInstance().getQueuesSize());
                            Toast.makeText(getContext(), "Successfully added to room's queue!", Toast.LENGTH_SHORT).show();
                        });
                    }, ()-> { //blad w trakcie dodawania do kolejki
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Something went wrong during adding to queue!", Toast.LENGTH_SHORT).show());
                    }},
                    TaskManager.nextCommunicationStream()
            ));
        });

        return rootView;
    }
}
