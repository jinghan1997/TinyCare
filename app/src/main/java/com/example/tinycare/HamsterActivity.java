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

public class HamsterActivity extends AppCompatActivity {

    private ActionBar bottomNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hamster);

        // Set up bottom navigation bar
        bottomNavigationBar = getSupportActionBar();
        BottomNavigationView navigation = findViewById(R.id.hamster_bottom_nav);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationBar.setTitle("HamsterCare");
        loadFragment(new HamsterMainFragment());

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.hamster_care:
                    bottomNavigationBar.setTitle("HamsterCare");
                    loadFragment(new HamsterMainFragment());
                    return true;
                case R.id.hamster_photo:
                    bottomNavigationBar.setTitle("Photo");
                    loadFragment(new HamsterPhotoFragment());
                    return true;
                case R.id.hamster_settings:
                    bottomNavigationBar.setTitle("Settings");
                    loadFragment(new HamsterSettingsFragment());
                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.hamster_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hamster, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.hamster_help) {
            Intent intent = new Intent(this, HamsterHelpActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.hamster_about) {
            Intent intent = new Intent(this, HamsterAboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
