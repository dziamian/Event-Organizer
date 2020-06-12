package com.example.eventorganizer;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TITLE = "title";

    // TODO: Rename and change types of parameters
    private String mTitle;

    /// TODO:
    private ItemListAdapter<SectorLayout> itemListAdapter = null;

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
    public static SectorFragment newInstance(String title) {
        SectorFragment fragment = new SectorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((HomeActivity) Objects.requireNonNull(getActivity())).rooms.setVisible(false);
        getActivity().setTitle(mTitle);
        ((HomeActivity)getActivity()).navigationView.setCheckedItem(R.id.nav_sectors);
        ((HomeActivity)getActivity()).setSelectedItemId(R.id.nav_sectors);

        View rootView = inflater.inflate(R.layout.fragment_sector, container, false);

        ArrayList<SectorLayout> sectorList = new ArrayList<>();
        TaskManager.eventInfoFixed.getSectors().values().forEach(sectorInfo -> sectorList.add(new SectorLayout(sectorInfo)));

        getActivity().runOnUiThread(() -> {
            ListView listView = rootView.findViewById(R.id.sector_list_view);
            itemListAdapter = new ItemListAdapter<>(getActivity(), sectorList);
            listView.setAdapter(itemListAdapter);
        });

        HomeActivity.setUpdatingUI(true);
        MainActivity.connectionToServer.addIncomingMessage(new BaseMessage(
                "update",
                null,
                (Runnable) () -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            for (int i = 0; i < TaskManager.eventInfoUpdate.getSectors().size(); ++i) {
                                itemListAdapter.layoutList.get(i).updateItemHolderAttributes(TaskManager.eventInfoUpdate);
                            }
                        });
                    }
                },
                TaskManager.nextCommunicationStream())
        );

        return rootView;
    }
}
