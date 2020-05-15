package com.example.eventorganizer;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mNavDrawer;
    public NavigationView navigationView;
    private int selectedItemId;

    public MenuItem rooms;

    public void setSelectedItemId(int id) {
        selectedItemId = id;
    }

    public void setRoomActivity(String sectorName) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_layout, SectorRoomsFragment.newInstance(sectorName))
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
        //rooms.setVisible(false);

        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_layout, SectorFragment.newInstance("Sektory"))
                    .commit();
            selectedItemId = R.id.nav_sectors;
        }
    }

    @Override
    public void onBackPressed() {
        if (mNavDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavDrawer.closeDrawer(GravityCompat.START);
        } else {
            /*if (rooms.isVisible()) {
                setTitle("Sektory");
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new SectorFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_sectors);
                rooms.setVisible(false);
            } else {
                super.onBackPressed();
            }*/
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_sectors: {
                if (selectedItemId != R.id.nav_sectors) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, SectorFragment.newInstance("Sektory")).commit();
                }
                mNavDrawer.closeDrawer(GravityCompat.START);
            } break;
            case R.id.nav_rooms: {
                if (selectedItemId != R.id.nav_rooms) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new SectorRoomsFragment()).commit();
                }
                mNavDrawer.closeDrawer(GravityCompat.START);
            } break;
            /// DO POPRAWY JAK WYŻEJ //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            case R.id.nav_queues: {
                if (selectedItemId != R.id.nav_queues) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    setTitle("Moje kolejki");
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new QueueFragment()).commit();
                    rooms.setVisible(false);
                }
                mNavDrawer.closeDrawer(GravityCompat.START);
            } break;
            case R.id.nav_reservations: {
                if (selectedItemId != R.id.nav_reservations) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    setTitle("Moje rezerwacje");
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new ReservationFragment()).commit();
                    rooms.setVisible(false);
                }
                mNavDrawer.closeDrawer(GravityCompat.START);
            } break;
        }
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
