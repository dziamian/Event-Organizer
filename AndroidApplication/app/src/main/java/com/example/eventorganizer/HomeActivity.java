package com.example.eventorganizer;

import android.graphics.Typeface;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import com.example.eventorganizer.fragments.*;
import com.google.android.material.navigation.NavigationView;
import org.bson.types.ObjectId;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Activity that is used after successful log in. Linking every {@link androidx.fragment.app.Fragment} subclasses from
 * <b>fragments</b> package.
 */
public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /** Static object that tells {@link TaskManager} to keep updating {@link network_structures.EventInfoUpdate} from server */
    private static final AtomicBoolean isUpdating = new AtomicBoolean(false);
    /** Static object that tells {@link TaskManager} to keep updating {@link network_structures.QueueInfo} from server */
    private static final AtomicBoolean isShowingTickets = new AtomicBoolean(false);

    /**
     * Returns the state of <b>isUpdating</b>.
     * @return State of <b>isUpdating</b>
     */
    public static synchronized boolean isUpdating() {
        return HomeActivity.isUpdating.get();
    }

    /**
     * Assigns the state of <b>isUpdating</b>.
     * @param status Current state to assign
     */
    public static synchronized void setUpdating(boolean status) {
        HomeActivity.isUpdating.set(status);
    }

    /**
     * Returns the state of <b>isShowingTickets</b>.
     * @return State of <b>isShowingTickets</b>
     */
    public static synchronized boolean isShowingTickets() {
        return HomeActivity.isShowingTickets.get();
    }

    /**
     * Assigns the state of <b>isShowingTickets</b>.
     * @param status Current state to assign
     */
    public static synchronized void setShowingTickets(boolean status) {
        HomeActivity.isShowingTickets.set(status);
    }

    /** Object that is responsible for drawing {@link NavigationView} and handle events related with it */
    private DrawerLayout mNavDrawer;
    /** Object that contains side menu */
    private NavigationView navigationView;
    /** Current selected item ID in side menu */
    private int selectedItemId;
    /** Badge for 'nav_queues' item in side menu */
    private TextView queuesBadge;
    /** Badge for 'nav_reservations' item in side menu */
    private TextView reservationBadge;

    /** Object that contains reference to 'nav_rooms' item in side menu */
    private MenuItem rooms;

    /**
     * Returns reference of <b>navigationView</b>.
     * @return Reference of <b>navigationView</b>
     */
    public NavigationView getNavigationView() { return navigationView; }

    /**
     * Assigns new selected item's ID of side menu.
     * @param id Selected item's ID
     */
    public void setSelectedItemId(int id) {
        selectedItemId = id;
    }

    /**
     * Assigns new number of queues (or empty text if this number is less than zero or equals) in 'nav_queue'
     * item of side menu.
     * @param number Current number of queues
     */
    public void setQueueBadgeText(int number) {
        if (number > 0) {
            queuesBadge.setText(String.valueOf(number));
        } else {
            queuesBadge.setText("");
        }
    }

    /**
     * Assigns new color from colors' resources to 'nav_queue' badge.
     * @param color Color identifier
     */
    public void setQueueBadgeColor(int color) { queuesBadge.setTextColor(getResources().getColor(color)); }

    /**
     * Assigns new number of reservations (or empty text if this number is less than zero or equals) in 'nav_reservation'
     * @param number Current number of reservations
     */
    public void setReservationBadgeText(int number) {
        if (number > 0) {
            reservationBadge.setText(String.valueOf(number));
        } else {
            reservationBadge.setText("");
        }
    }

    /**
     * Returns reference to rooms menu item.
     * @return Reference to rooms menu item
     */
    public MenuItem getRooms() { return rooms; }

    /**
     * Uses provided sectorId to set new {@link SectorRoomsFragment}.
     * @param sectorId Sector ID of choice
     */
    public void setSectorRoomsFragment(ObjectId sectorId) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_layout, SectorRoomsFragment.newInstance(sectorId.toString()))
                .addToBackStack(null)
                .commit();
    }

    /**
     * Uses provided identifiers to set new {@link RoomFragment}.
     * @param sectorId Sector ID of choice
     * @param roomId Room ID of choice
     */
    public void setRoomFragment(ObjectId sectorId, ObjectId roomId) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_layout, RoomFragment.newInstance(sectorId.toString(), roomId.toString()))
                .addToBackStack(null)
                .commit();
    }

    /**
     * Method used for initializing activity by creating UI elements and {@link SectorFragment} instance.
     * @param savedInstanceState Contains the data which was supplied in {@link android.app.Activity#onSaveInstanceState(Bundle)}
     *                           (currently not in use)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.nav_drawer_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNavDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mNavDrawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        mNavDrawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.navigation_view);
        int width = getResources().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams params = navigationView.getLayoutParams();
        params.width = (int) (width * 0.8);
        navigationView.setLayoutParams(params);

        rooms = navigationView.getMenu().findItem(R.id.nav_rooms);

        queuesBadge = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_queues));
        queuesBadge.setGravity(Gravity.CENTER_VERTICAL);
        queuesBadge.setTypeface(null, Typeface.BOLD);
        queuesBadge.setTextColor(getResources().getColor(R.color.colorBadgeUnselected));

        reservationBadge = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_reservations));
        reservationBadge.setGravity(Gravity.CENTER_VERTICAL);
        reservationBadge.setTypeface(null, Typeface.BOLD);
        reservationBadge.setTextColor(getResources().getColor(R.color.colorBadgeImportant));

        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_layout, SectorFragment.newInstance())
                    .commit();
            selectedItemId = R.id.nav_sectors;
        }

        CurrentSession.getInstance().setHomeActivity(this);
    }

    /**
     * Callback for back button.
     */
    @Override
    public void onBackPressed() {
        if (mNavDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * General callback for activating various menu items.
     * @param menuItem Item which invoked callback
     * @return Always true
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_sectors: {
                if (selectedItemId != R.id.nav_sectors) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, SectorFragment.newInstance()).commit();
                }
            } break;
            case R.id.nav_queues: {
                if (selectedItemId != R.id.nav_queues) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, QueueFragment.newInstance()).commit();
                }
            } break;
            case R.id.nav_reservations: {
                if (selectedItemId != R.id.nav_reservations) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, ReservationFragment.newInstance()).commit();
                }
            } break;
        }
        mNavDrawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
