package com.example.eventorganizer;

import android.os.Bundle;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import network_structures.ReservationInfo;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReservationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReservationFragment extends Fragment {

    private ArrayList<ReservationLayout> reservationList;
    private ItemListAdapter<ReservationLayout> itemListAdapter;

    public ReservationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ReservationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReservationFragment newInstance() {
        return new ReservationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeActivity.setUpdating(false);
        HomeActivity.setShowingTickets(false);

        ((HomeActivity) Objects.requireNonNull(getActivity())).getRooms().setVisible(false);
        getActivity().setTitle("Moje rezerwacje");
        ((HomeActivity) getActivity()).getNavigationView().setCheckedItem(R.id.nav_reservations);
        ((HomeActivity) getActivity()).setSelectedItemId(R.id.nav_reservations);
        ((HomeActivity) getActivity()).setQueueBadgeColor(R.color.colorBadgeUnselected);

        View rootView = inflater.inflate(R.layout.fragment_reservation, container, false);

        reservationList = new ArrayList<>();

        itemListAdapter = new ItemListAdapter<>(getActivity(), reservationList);

        ReservationInfo reservationInfo = GuideAccount.getInstance().getReservationInfo();
        if (reservationInfo != null) {
            ReservationLayout reservationLayout = new ReservationLayout(reservationInfo);
            reservationList.add(reservationLayout);
            new Thread(() -> {
                long expirationTime = reservationInfo.getExpirationDate().getTime();
                while (getActivity() != null && GuideAccount.getInstance().getReservationInfo() != null) {
                    getActivity().runOnUiThread(() -> reservationLayout.updateTime((expirationTime - System.currentTimeMillis()) / 1000));
                }
                reservationList.clear();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> itemListAdapter.notifyDataSetChanged());
                }
            }).start();
        }

        getActivity().runOnUiThread(() -> {
            ListView listView = rootView.findViewById(R.id.reservation_list_view);
            listView.setAdapter(itemListAdapter);
        });

        return rootView;
    }
}
