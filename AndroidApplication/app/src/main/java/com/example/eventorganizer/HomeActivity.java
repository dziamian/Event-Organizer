package com.example.eventorganizer;

import android.graphics.Typeface;
import android.util.TypedValue;
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
import com.google.android.material.navigation.NavigationView;
import org.bson.types.ObjectId;

import java.util.concurrent.atomic.AtomicBoolean;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final AtomicBoolean updatingUI = new AtomicBoolean(true);

    public static synchronized boolean getUpdatingUI() {
        return HomeActivity.updatingUI.get();
    }

    public static synchronized void setUpdatingUI(boolean status) {
        HomeActivity.updatingUI.set(status);
    }

    private DrawerLayout mNavDrawer;
    private NavigationView navigationView;
    private int selectedItemId;
    private TextView queueBadge;

    private MenuItem rooms;

    public NavigationView getNavigationView() { return navigationView; }

    public void setSelectedItemId(int id) {
        selectedItemId = id;
    }

    public void setQueueBadgeText(String text) {
        queueBadge.setText(text);
    }

    public void setQueueBadgeColor(int color) { queueBadge.setTextColor(getResources().getColor(color)); }

    public MenuItem getRooms() { return rooms; }

    public void setSectorRoomsFragment(ObjectId sectorId) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_layout, SectorRoomsFragment.newInstance(sectorId.toString()))
                .addToBackStack(null)
                .commit();
    }

    public void setRoomActivity(ObjectId sectorId, ObjectId roomId) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_layout, RoomFragment.newInstance(sectorId.toString(), roomId.toString()))
                .addToBackStack(null)
                .commit();
    }

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

        queueBadge = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_queues));
        queueBadge.setGravity(Gravity.CENTER_VERTICAL);
        queueBadge.setTypeface(null, Typeface.BOLD);
        queueBadge.setTextColor(getResources().getColor(R.color.colorBadgeUnselected));

        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_layout, SectorFragment.newInstance())
                    .commit();
            selectedItemId = R.id.nav_sectors;
        }
    }

    @Override
    public void onBackPressed() {
        if (mNavDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_sectors: {
                if (selectedItemId != R.id.nav_sectors) {
                    setUpdatingUI(false);
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, SectorFragment.newInstance()).commit();
                }
            } break;
            case R.id.nav_queues: {
                if (selectedItemId != R.id.nav_queues) {
                    setUpdatingUI(false);
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, QueueFragment.newInstance()).commit();
                }
            } break;
            /// DO POPRAWY JAK WYŻEJ //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case R.id.nav_reservations: {
                if (selectedItemId != R.id.nav_reservations) {
                    setQueueBadgeColor(R.color.colorBadgeUnselected);
                    /*getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    setTitle("Moje rezerwacje");
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new ReservationFragment()).commit();
                    rooms.setVisible(false);*/
                }
            } break;
        }
        mNavDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
