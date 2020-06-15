package com.example.eventorganizer.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.eventorganizer.*;
import com.example.eventorganizer.list.ItemListAdapter;
import com.example.eventorganizer.list.QueueLayout;
import network_structures.BaseMessage;
import network_structures.QueueInfo;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass responsible for displaying queues information.
 */
public class QueueFragment extends Fragment {

    /** List with current queues */
    private ArrayList<QueueLayout> queuesList;
    /** Adapter which is used by ListView */
    private ItemListAdapter<QueueLayout> itemListAdapter;

    /**
     * Default constructor required by API.
     */
    public QueueFragment() {

    }

    /**
     * Creates new instance of this fragment.
     * @return A new instance of fragment QueueFragment
     */
    public static QueueFragment newInstance() {
        return new QueueFragment();
    }

    /**
     * Procedure responsible for scheduling and handling 'view_tickets' request.
     * @param activity Current activity of application
     */
    private void viewTickets(Activity activity) {
        MainActivity.taskManager.addIncomingMessage(new BaseMessage(
                "view_tickets",
                null,
                (Runnable) () -> {
                    activity.runOnUiThread(() -> {
                        queuesList.clear();
                        QueueInfo[] queueArray = CurrentSession.getInstance().getQueues();
                        if (queueArray != null) {
                            for (QueueInfo queueInfo : queueArray) {
                                QueueLayout queueLayout = new QueueLayout(queueInfo);
                                queueLayout.setLeavingQueueMessage(new BaseMessage(
                                        "remove_from_queue",
                                        new String[] { queueInfo.getSectorId().toString(), queueInfo.getRoomId().toString() },
                                        new Runnable[] { () -> { //poprawnie usunieto z kolejki
                                            activity.runOnUiThread(() -> {
                                                ((HomeActivity) activity).setQueueBadgeText(CurrentSession.getInstance().getNumberOfQueues());
                                                Toast.makeText(getContext(), "Successfully removed from the queue!", Toast.LENGTH_SHORT).show();
                                            });
                                        }, () -> { //blad w trakcie usuwania z kolejki
                                            activity.runOnUiThread(() -> {
                                                Toast.makeText(getContext(), "Something went wrong during removing from the queue!", Toast.LENGTH_SHORT).show();
                                            });
                                        }},
                                        TaskManager.nextCommunicationStream()
                                ));
                                queuesList.add(queueLayout);
                            }
                        }
                        itemListAdapter.notifyDataSetChanged();
                    });
                },
                TaskManager.nextCommunicationStream())
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeActivity.setUpdating(false);
        HomeActivity.setShowingTickets(false);

        ((HomeActivity) Objects.requireNonNull(getActivity())).getRooms().setVisible(false);
        getActivity().setTitle("Moje kolejki");
        ((HomeActivity)getActivity()).getNavigationView().setCheckedItem(R.id.nav_queues);
        ((HomeActivity)getActivity()).setSelectedItemId(R.id.nav_queues);
        ((HomeActivity)getActivity()).setQueueBadgeColor(R.color.colorPrimary);

        View rootView = inflater.inflate(R.layout.fragment_queue, container, false);

        queuesList = new ArrayList<>();

        itemListAdapter = new ItemListAdapter<>(getActivity(), queuesList);

        getActivity().runOnUiThread(() -> {
            ListView listView = rootView.findViewById(R.id.queue_list_view);
            listView.setAdapter(itemListAdapter);
        });

        HomeActivity.setShowingTickets(true);
        viewTickets(getActivity());

        return rootView;
    }
}
