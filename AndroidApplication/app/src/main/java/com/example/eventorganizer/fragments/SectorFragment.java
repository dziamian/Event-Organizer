package com.example.eventorganizer.fragments;

import android.os.Bundle;
import android.widget.*;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.eventorganizer.*;
import com.example.eventorganizer.list.ItemListAdapter;
import com.example.eventorganizer.list.SectorLayout;
import network_structures.BaseMessage;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass responsible for displaying sectors information.
 */
public class SectorFragment extends Fragment {

    /**
     * Default constructor required by API.
     */
    public SectorFragment() {

    }

    /**
     * Creates new instance of this fragment.
     * @return A new instance of fragment SectorFragment
     */
    public static SectorFragment newInstance() {
        return new SectorFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeActivity.setUpdating(false);
        HomeActivity.setShowingTickets(false);

        ((HomeActivity) Objects.requireNonNull(getActivity())).getRooms().setVisible(false);
        getActivity().setTitle("Sektory");
        ((HomeActivity)getActivity()).getNavigationView().setCheckedItem(R.id.nav_sectors);
        ((HomeActivity)getActivity()).setSelectedItemId(R.id.nav_sectors);
        ((HomeActivity)getActivity()).setQueueBadgeColor(R.color.colorBadgeUnselected);

        View rootView = inflater.inflate(R.layout.fragment_sector, container, false);

        ArrayList<SectorLayout> sectorList = new ArrayList<>();
        CurrentSession.getInstance().getEventInfoFixed().getSectors().values().forEach(sectorInfo -> sectorList.add(new SectorLayout(sectorInfo)));

        ItemListAdapter<SectorLayout> itemListAdapter = new ItemListAdapter<>(getActivity(), sectorList);

        getActivity().runOnUiThread(() -> {
            ListView listView = rootView.findViewById(R.id.sector_list_view);
            listView.setAdapter(itemListAdapter);
        });

        HomeActivity.setUpdating(true);
        MainActivity.taskManager.addIncomingMessage(new BaseMessage(
                "update",
                null,
                (Runnable) () -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            for (int i = 0; i < CurrentSession.getInstance().getEventInfoUpdate().getSectors().size(); ++i) {
                                itemListAdapter.getLayoutList().get(i).updateItemHolderAttributes(CurrentSession.getInstance().getEventInfoUpdate());
                            }
                        });
                    }
                },
                TaskManager.nextCommunicationStream())
        );

        return rootView;
    }
}
