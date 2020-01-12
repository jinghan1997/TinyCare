package com.example.tinycare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    final String KEY_DATA = "data";
    final String PREF_FILE = "mainsharedpref";
    SharedPreferences mPreferences;

    final static int REQUEST_NEW_PET = 2000;
    DataSource dataSource;
    RecyclerView recyclerView;
    PetAdapter petAdapter;

    String id;
    String type;
    String name;
    String path;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references to widgets
        recyclerView = findViewById(R.id.petRecyclerView);

        // Set up floating action button to add new pet
        FloatingActionButton fab = findViewById(R.id.main_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DataEntry.class);
                startActivityForResult(intent, REQUEST_NEW_PET);
            }
        });

        // Load the Json string from shared Preferences
        mPreferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        String json = mPreferences.getString(KEY_DATA, "");
        if( !json.isEmpty() ){
            Gson gson = new Gson();
            dataSource = gson.fromJson(json, DataSource.class);
        } else {
            dataSource = new DataSource();
        }

        // Set up RecyclerView to display pets
        petAdapter = new PetAdapter(this, dataSource);
        recyclerView.setAdapter(petAdapter);
        recyclerView.setLayoutManager(
                new GridLayoutManager(this, 1));

        // Delete pet from RecyclerView upon swiping
        ItemTouchHelper.SimpleCallback simpleCallback = new
                ItemTouchHelper.SimpleCallback( 0 , ItemTouchHelper.LEFT |
                        ItemTouchHelper.RIGHT ) {
                    @Override
                    public boolean onMove (@NonNull RecyclerView recyclerView,
                                           @NonNull RecyclerView.ViewHolder viewHolder, @NonNull
                                                   RecyclerView.ViewHolder viewHolder1) {
                        return false ;
                    }
                    @Override
                    public void onSwiped (@NonNull RecyclerView.ViewHolder
                                                  viewHolder, int i) {
                        //code to delete the view goes here
                        PetAdapter.PetViewHolder petViewHolder
                                = (PetAdapter.PetViewHolder) viewHolder;
                        int position = petViewHolder.getAdapterPosition();
                        String petRemoved = dataSource.getName(position);
                        dataSource.removeData(position);
                        Toast.makeText(MainActivity.this,
                                petRemoved + " removed!", Toast.LENGTH_LONG).show();
                        petAdapter.notifyDataSetChanged();
                    }
                };
        ItemTouchHelper itemTouchHelper
                = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == REQUEST_NEW_PET && resultCode == Activity.RESULT_OK){
            id = data.getStringExtra(DataEntry.KEY_ID);
            // Retrieve type from Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference rootRef = database.getReference();
            Log.e("FIREBASE", "this line ran");
            ValueEventListener vel = rootRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.e("FIREBASE", "type extracted?");
                    type = dataSnapshot.child(id).child("type").getValue(String.class);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("FIREBASE", "DIED");
                    Log.e("ERROR", databaseError.getMessage());
                }
            });
            System.out.println(vel);
            if (type.equals("fish")) {
                name = "fish";
                bitmap = Utils.drawableToBitmap(getResources().getDrawable(R.drawable.ic_clown_fish_svgrepo_com));
                path = Utils.saveToInternalStorage(bitmap, name, MainActivity.this);
            } else if (type.equals("plant")) {
                name = "plant";
                bitmap = Utils.drawableToBitmap(getResources().getDrawable(R.drawable.ic_plant_svgrepo_com));
                path = Utils.saveToInternalStorage(bitmap, name, MainActivity.this);
            } else {
                name = "hamster";
                bitmap = Utils.drawableToBitmap(getResources().getDrawable(R.drawable.ic_hamster_svgrepo_com));
                path = Utils.saveToInternalStorage(bitmap, name, MainActivity.this);
            }
            dataSource.addData(name, path, type, id);
            petAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        SharedPreferences.Editor prefsEditor = mPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(dataSource);
        prefsEditor.putString(KEY_DATA,json);
        prefsEditor.apply();
    }
}
