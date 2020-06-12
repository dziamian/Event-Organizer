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
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mSectorId;

    private ItemListAdapter<RoomLayout> itemListAdapter = null;

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
        args.putString(ARG_PARAM1, sectorId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSectorId = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ObjectId sectorId = new ObjectId(mSectorId);
        SectorInfoFixed sectorInfoFixed = TaskManager.eventInfoFixed.getSectors().get(sectorId);

        ((HomeActivity) Objects.requireNonNull(getActivity())).rooms.setVisible(true);
        ((HomeActivity) getActivity()).rooms.setTitle("Sektory - " + sectorInfoFixed.getName());
        getActivity().setTitle(sectorInfoFixed.getName());
        ((HomeActivity) getActivity()).navigationView.setCheckedItem(R.id.nav_rooms);
        ((HomeActivity) getActivity()).setSelectedItemId(R.id.nav_rooms);

        View rootView = inflater.inflate(R.layout.fragment_sector_rooms, container, false);

        ArrayList<RoomLayout> roomList = new ArrayList<>();
        sectorInfoFixed.getRooms().values().forEach(roomInfo -> roomList.add(new RoomLayout(roomInfo)));

        getActivity().runOnUiThread(() -> {
            ListView listView = rootView.findViewById(R.id.room_list_view);
            itemListAdapter = new ItemListAdapter<>(getActivity(), roomList);
            listView.setAdapter(itemListAdapter);
        });

        HomeActivity.setUpdatingUI(true);
        MainActivity.connectionToServer.addIncomingMessage(new BaseMessage(
                "update",
                null,
                (Runnable) () -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            int numberOfRooms = TaskManager.eventInfoUpdate.getSectors().get(sectorId).getRooms().size();
                            for (int i = 0; i < numberOfRooms; ++i) {
                                itemListAdapter.layoutList.get(i).updateItemHolderAttributes(TaskManager.eventInfoUpdate, sectorId);
                            }
                        });
                    }
                },
                TaskManager.nextCommunicationStream()
        ));

        return rootView;
    }
}
