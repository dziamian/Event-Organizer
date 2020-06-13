package com.example.eventorganizer;

import android.os.Bundle;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import network_structures.BaseMessage;
import org.bson.types.ObjectId;

import network_structures.SectorInfoFixed;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SectorRoomsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SectorRoomsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_SECTOR_ID = "sectorID";

    // TODO: Rename and change types of parameters
    private String sectorId;

    public SectorRoomsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment SectorRoomsFragment.
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
        ObjectId sectorId = new ObjectId(this.sectorId);
        SectorInfoFixed sectorInfoFixed = GuideAccount.getInstance().getEventInfoFixed().getSectors().get(sectorId);

        ((HomeActivity) Objects.requireNonNull(getActivity())).rooms.setVisible(true);
        ((HomeActivity) getActivity()).rooms.setTitle(sectorInfoFixed.getName());
        getActivity().setTitle(sectorInfoFixed.getName());
        ((HomeActivity) getActivity()).navigationView.setCheckedItem(R.id.nav_rooms);
        ((HomeActivity) getActivity()).setSelectedItemId(R.id.nav_rooms);

        View rootView = inflater.inflate(R.layout.fragment_sector_rooms, container, false);

        ArrayList<SectorRoomLayout> roomList = new ArrayList<>();
        sectorInfoFixed.getRooms().values().forEach(roomInfo -> roomList.add(new SectorRoomLayout(roomInfo)));

        ItemListAdapter<SectorRoomLayout> itemListAdapter = new ItemListAdapter<>(getActivity(), roomList);

        getActivity().runOnUiThread(() -> {
            ListView listView = rootView.findViewById(R.id.room_list_view);
            listView.setAdapter(itemListAdapter);
        });

        HomeActivity.setUpdatingUI(true);
        MainActivity.connectionToServer.addIncomingMessage(new BaseMessage(
                "update",
                null,
                (Runnable) () -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            int numberOfRooms = GuideAccount.getInstance().getEventInfoUpdate().getSectors().get(sectorId).getRooms().size();
                            for (int i = 0; i < numberOfRooms; ++i) {
                                itemListAdapter.layoutList.get(i).updateItemHolderAttributes(GuideAccount.getInstance().getEventInfoUpdate(), sectorId);
                            }
                        });
                    }
                },
                TaskManager.nextCommunicationStream()
        ));

        return rootView;
    }
}
