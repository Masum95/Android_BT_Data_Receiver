package com.samsung.android.sdk.accessory.example.filetransfer.receiver;


import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import static com.samsung.android.sdk.accessory.example.filetransfer.receiver.Constants.SHARED_PREF_ID;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        prefs = getSharedPreferences(SHARED_PREF_ID, 0);
        boolean has_medical_profile_registered = prefs.getBoolean("HAS_MEDICAL_PROFILE_REGISTERED", false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            if (!has_medical_profile_registered) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MedicalProfileRegisterFragment1()).commit();
                navigationView.setCheckedItem(R.id.profile_registration);

            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FileTransferReceiverFragment()).commit();
                navigationView.setCheckedItem(R.id.dashboard);
            }

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile_registration:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MedicalProfileRegisterFragment1()).commit();
                break;
            case R.id.dashboard:

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FileTransferReceiverFragment()).commit();

                break;
            case R.id.records:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ExportPdfFragment()).commit();

                break;

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}