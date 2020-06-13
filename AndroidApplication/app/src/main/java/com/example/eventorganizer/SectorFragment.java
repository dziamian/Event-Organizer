package com.example.eventorganizer;

import android.os.Bundle;
import android.widget.*;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import network_structures.BaseMessage;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SectorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SectorFragment extends Fragment {

    /// TODO:
    public SectorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param
     * @return A new instance of fragment SectorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SectorFragment newInstance() {
        return new SectorFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((HomeActivity) Objects.requireNonNull(getActivity())).rooms.setVisible(false);
        getActivity().setTitle("Sektory");
        ((HomeActivity)getActivity()).navigationView.setCheckedItem(R.id.nav_sectors);
        ((HomeActivity)getActivity()).setSelectedItemId(R.id.nav_sectors);

        View rootView = inflater.inflate(R.layout.fragment_sector, container, false);

        ArrayList<SectorLayout> sectorList = new ArrayList<>();
        GuideAccount.getInstance().getEventInfoFixed().getSectors().values().forEach(sectorInfo -> sectorList.add(new SectorLayout(sectorInfo)));

        ItemListAdapter<SectorLayout> itemListAdapter = new ItemListAdapter<>(getActivity(), sectorList);

        getActivity().runOnUiThread(() -> {
            ListView listView = rootView.findViewById(R.id.sector_list_view);
            listView.setAdapter(itemListAdapter);
        });

        HomeActivity.setUpdatingUI(true);
        MainActivity.taskManager.addIncomingMessage(new BaseMessage(
                "update",
                null,
                (Runnable) () -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            for (int i = 0; i < GuideAccount.getInstance().getEventInfoUpdate().getSectors().size(); ++i) {
                                itemListAdapter.layoutList.get(i).updateItemHolderAttributes(GuideAccount.getInstance().getEventInfoUpdate());
                            }
                        });
                    }
                },
                TaskManager.nextCommunicationStream())
        );

        return rootView;
    }
}
