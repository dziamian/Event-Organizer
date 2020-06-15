package com.example.eventorganizer.fragments;

import android.os.Bundle;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.eventorganizer.*;
import com.example.eventorganizer.list.ItemListAdapter;
import com.example.eventorganizer.list.SectorRoomLayout;
import network_structures.BaseMessage;
import org.bson.types.ObjectId;

import network_structures.SectorInfoFixed;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass responsible for displaying sector's rooms information.
 */
public class SectorRoomsFragment extends Fragment {
    /** Bundle's argument name */
    private static final String ARG_SECTOR_ID = "sectorID";

    /** Sector ID */
    private String sectorId;

    /**
     * Default constructor required by API.
     */
    public SectorRoomsFragment() {

    }

    /**
     * Creates new instance of this fragment.
     * @param sectorId Sector ID
     * @return A new instance of fragment SectorRoomFragment
     */
    public static SectorRoomsFragment newInstance(String sectorId) {
        SectorRoomsFragment fragment = new SectorRoomsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SECTOR_ID, sectorId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sectorId = getArguments().getString(ARG_SECTOR_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeActivity.setUpdating(false);
        HomeActivity.setShowingTickets(false);

        ObjectId sectorId = new ObjectId(this.sectorId);
        SectorInfoFixed sectorInfoFixed = CurrentSession.getInstance().getEventInfoFixed().getSectors().get(sectorId);

        ((HomeActivity) Objects.requireNonNull(getActivity())).getRooms().setVisible(true);
        ((HomeActivity) getActivity()).getRooms().setTitle(sectorInfoFixed.getName());
        getActivity().setTitle(sectorInfoFixed.getName());
        ((HomeActivity) getActivity()).getNavigationView().setCheckedItem(R.id.nav_rooms);
        ((HomeActivity) getActivity()).setSelectedItemId(R.id.nav_rooms);

        View rootView = inflater.inflate(R.layout.fragment_sector_rooms, container, false);

        ArrayList<SectorRoomLayout> roomList = new ArrayList<>();
        sectorInfoFixed.getRooms().values().forEach(roomInfo -> roomList.add(new SectorRoomLayout(roomInfo)));

        ItemListAdapter<SectorRoomLayout> itemListAdapter = new ItemListAdapter<>(getActivity(), roomList);

        getActivity().runOnUiThread(() -> {
            ListView listView = rootView.findViewById(R.id.room_list_view);
            listView.setAdapter(itemListAdapter);
        });

        HomeActivity.setUpdating(true);
        MainActivity.taskManager.addIncomingMessage(new BaseMessage(
                "update",
                null,
                (Runnable) () -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            int numberOfRooms = CurrentSession.getInstance().getEventInfoUpdate().getSectors().get(sectorId).getRooms().size();
                            for (int i = 0; i < numberOfRooms; ++i) {
                                itemListAdapter.getLayoutList().get(i).updateItemHolderAttributes(CurrentSession.getInstance().getEventInfoUpdate(), sectorId);
                            }
                        });
                    }
                },
                TaskManager.nextCommunicationStream()
        ));

        return rootView;
    }
}
