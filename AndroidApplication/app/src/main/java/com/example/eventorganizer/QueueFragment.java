package com.example.eventorganizer;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QueueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QueueFragment extends Fragment {

    public QueueFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment QueueFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QueueFragment newInstance() {
        return new QueueFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((HomeActivity) Objects.requireNonNull(getActivity())).rooms.setVisible(false);
        getActivity().setTitle("Moje kolejki");
        ((HomeActivity)getActivity()).navigationView.setCheckedItem(R.id.nav_queues);
        ((HomeActivity)getActivity()).setSelectedItemId(R.id.nav_queues);

        View rootView = inflater.inflate(R.layout.fragment_queue, container, false);

        ArrayList<QueueLayout> queuesList = new ArrayList<>();


        return rootView;
    }
}
