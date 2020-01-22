package com.example.tinycare;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PlantActivity extends AppCompatActivity {

    private ActionBar bottomNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant);

        // Set up bottom navigation bar
        bottomNavigationBar = getSupportActionBar();
        BottomNavigationView navigation = findViewById(R.id.plant_bottom_nav);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationBar.setTitle("PlantCare");
        loadFragment(new PlantMainFragment());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.plant_care:
                    bottomNavigationBar.setTitle("PlantCare");
                    loadFragment(new PlantMainFragment());
                    return true;
                case R.id.plant_photo:
                    bottomNavigationBar.setTitle("Photo");
                    loadFragment(new PlantPhotoFragment());
                    return true;
                case R.id.plant_settings:
                    bottomNavigationBar.setTitle("Settings");
                    loadFragment(new PlantSettingsFragment());
                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.plant_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_plant, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.plant_help) {
            Intent intent = new Intent(this, PlantHelpActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.plant_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
