package com.example.eventorganizer;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import network_structures.SectorUpdate;

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
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mTitle;
    private ArrayList<SectorLayout> sectorLayouts;

    private void updateInterface() {
        while (ClientConnection.updateData == null) { }
        while (sectorLayouts.get(0).sectorLayoutHolder == null) { }
        while (true) {
            int i = 0;
            for (SectorUpdate update : ClientConnection.updateData.sectors.values()) {
                sectorLayouts.get(i++).updateLayout(update);
            }
        }
    }

    public SectorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SectorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SectorFragment newInstance(String title) {
        SectorFragment fragment = new SectorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        /*Log.d("LIST", itemListAdapter.layoutList.get(1).sectorLayoutHolder.textViewName.getText().toString());
        for (SectorLayout sectorLayout : itemListAdapter.layoutList) {
            Log.d("LIST", sectorLayout.sectorLayoutHolder.textViewName.getText().toString());
        }*/
        //new Thread(this::updateInterface).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((HomeActivity) Objects.requireNonNull(getActivity())).rooms.setVisible(false);
        Objects.requireNonNull(getActivity()).setTitle(mTitle);
        ((HomeActivity)getActivity()).navigationView.setCheckedItem(R.id.nav_sectors);
        ((HomeActivity) getActivity()).setSelectedItemId(R.id.nav_sectors);

        View rootView = inflater.inflate(R.layout.fragment_sector, container, false);

        ArrayList<SectorLayout> sectorList = new ArrayList<>();
        ClientConnection.eventData.sectors.values().forEach(sectorInfo -> sectorList.add(new SectorLayout(sectorInfo)));
        sectorLayouts = sectorList;

        ListView listView = rootView.findViewById(R.id.sector_list_view);
        ItemListAdapter<SectorLayout> itemListAdapter = new ItemListAdapter<>(getActivity(), sectorList);
        listView.setAdapter(itemListAdapter);

        //Log.d("TEST", sectorList.get(0).sectorLayoutHolder.textViewName.getText().toString());

        return rootView;
    }
}
