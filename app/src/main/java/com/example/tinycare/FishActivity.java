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

public class FishActivity extends AppCompatActivity {

    private ActionBar bottomNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish);

        // Set up bottom navigation bar
        bottomNavigationBar = getSupportActionBar();
        BottomNavigationView navigation = findViewById(R.id.fish_bottom_nav);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationBar.setTitle("FishCare");
        loadFragment(new FishMainFragment());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.fish_care:
                    bottomNavigationBar.setTitle("FishCare");
                    loadFragment(new FishMainFragment());
                    return true;
                case R.id.fish_photo:
                    bottomNavigationBar.setTitle("Photo");
                    loadFragment(new FishPhotoFragment());
                    return true;
                case R.id.fish_settings:
                    bottomNavigationBar.setTitle("Settings");
                    loadFragment(new FishSettingsFragment());
                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fish_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.fish_help) {
            Intent intent = new Intent(this, FishHelpActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.fish_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
