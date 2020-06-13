package com.example.eventorganizer;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import network_structures.BaseMessage;
import network_structures.QueueInfo;

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
        //GuideAccount.getInstance().getQueues().forEach(queueInfo -> queuesList.add(new QueueLayout(queueInfo)));

        ItemListAdapter<QueueLayout> itemListAdapter = new ItemListAdapter<>(getActivity(), queuesList);

        getActivity().runOnUiThread(() -> {
            ListView listView = rootView.findViewById(R.id.queue_list_view);
            listView.setAdapter(itemListAdapter);
        });

        MainActivity.connectionToServer.addIncomingMessage(new BaseMessage(
                "view_tickets",
                null,
                (Runnable) () -> {
                    getActivity().runOnUiThread(() -> {
                        queuesList.clear();
                        QueueInfo[] queueArray = GuideAccount.getInstance().getQueues();
                        Log.d("co jest", "aha");
                        if (queueArray != null) {
                            for (QueueInfo queueInfo : queueArray) {
                                Log.d("wtf", "wtf");
                                queuesList.add(new QueueLayout(queueInfo));
                            }
                            itemListAdapter.notifyDataSetChanged();
                        }
                    });
                },
                TaskManager.nextCommunicationStream())
        );

        //itemListAdapter.notifyDataSetChanged();

        return rootView;
    }
}
