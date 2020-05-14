package com.example.eventorganizer;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mSectorName;

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
    public static SectorRoomsFragment newInstance(String sectorName) {
        SectorRoomsFragment fragment = new SectorRoomsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, sectorName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSectorName = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((HomeActivity) Objects.requireNonNull(getActivity())).rooms.setVisible(true);
        ((HomeActivity) getActivity()).rooms.setTitle("Sektory - " + mSectorName);
        getActivity().setTitle(mSectorName);
        ((HomeActivity) getActivity()).navigationView.setCheckedItem(R.id.nav_rooms);
        ((HomeActivity) getActivity()).setSelectedItemId(R.id.nav_rooms);
        return inflater.inflate(R.layout.fragment_sector_rooms, container, false);
    }
}
