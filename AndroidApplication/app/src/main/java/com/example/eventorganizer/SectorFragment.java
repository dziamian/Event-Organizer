package com.example.eventorganizer;

import android.os.Bundle;
import android.text.Layout;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import network_structures.SectorInfo;

import java.util.ArrayList;
import java.util.Objects;

import static android.view.View.inflate;


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

    private void createSector() {

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
        FrameLayout layout = Objects.requireNonNull(getView()).findViewById(R.id.sector_layout);
        //RelativeLayout sectorField = getView().findViewById(R.id.first_element);
        //LayoutInflater layoutInflater = getLayoutInflater();
        //TextView sectorName = getView().findViewById(R.id.first_element_name);

        //layout.addView(new View(getContext()));

        //sectorField.setOnClickListener(v -> {
            //((HomeActivity) Objects.requireNonNull(getActivity())).setRoomActivity(sectorName.getText().toString());
        //});
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((HomeActivity) Objects.requireNonNull(getActivity())).rooms.setVisible(false);
        Objects.requireNonNull(getActivity()).setTitle(mTitle);
        ((HomeActivity)getActivity()).navigationView.setCheckedItem(R.id.nav_sectors);
        ((HomeActivity) getActivity()).setSelectedItemId(R.id.nav_sectors);

        View rootView = inflater.inflate(R.layout.fragment_sector, container, false);

        /*sectorList.add(new SectorLayout("Sektor A", "al. Tysiąclecia Państwa Polskiego 7", 25));
        sectorList.add(new SectorLayout("Sektor B", "al. Tysiąclecia Państwa Polskiego 7", 10));
        sectorList.add(new SectorLayout("Sektor C", "al. Tysiąclecia Państwa Polskiego 7", 8));

        sectorList.add(new SectorLayout("Sektor C", "al. Tysiąclecia Państwa Polskiego 7", 8));
        sectorList.add(new SectorLayout("Sektor C", "al. Tysiąclecia Państwa Polskiego 7", 8));
        sectorList.add(new SectorLayout("Sektor C", "al. Tysiąclecia Państwa Polskiego 7", 8));*/

        ArrayList<SectorInfo> sectorList = new ArrayList<>(ClientConnection.eventData.sectors.values());

        ListView listView = rootView.findViewById(R.id.sector_list_view);
        listView.setAdapter(new CustomListAdapter(getActivity(), sectorList));

        return rootView;
    }
}
