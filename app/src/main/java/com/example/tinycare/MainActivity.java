package com.example.tinycare;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
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

    PetDataSource petDataSource;
    RecyclerView recyclerView;
    PetAdapter petAdapter;

    final static int REQUEST_NEW_PET = 2000;
    String id;
    String type;
    String name;
    String path;
    Bitmap bitmap;

    // Seems like the reason why it wasn't calling the listeners was because Java's garbage collection threw away the references, and hence the event listeners
    // Making the variables "global" seems to work
    FirebaseDatabase database;
    DatabaseReference rootRef;

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
                Intent intent = new Intent(MainActivity.this, PetDataEntry.class);
                startActivityForResult(intent, REQUEST_NEW_PET);
            }
        });

        // Load the Json string from shared Preferences
        mPreferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        String json = mPreferences.getString(KEY_DATA, "");
        if( !json.isEmpty() ){
            Gson gson = new Gson();
            petDataSource = gson.fromJson(json, PetDataSource.class);
        } else {
            petDataSource = new PetDataSource();
        }

        // Set up RecyclerView to display pets
        petAdapter = new PetAdapter(this, petDataSource);
        recyclerView.setAdapter(petAdapter);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));

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
                        String petRemoved = petDataSource.getName(position);
                        petDataSource.removeData(position);
                        Toast.makeText(MainActivity.this,
                                petRemoved + " removed!", Toast.LENGTH_SHORT).show();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == REQUEST_NEW_PET && resultCode == Activity.RESULT_OK){
            id = data.getStringExtra(PetDataEntry.KEY_ID);
            // Retrieve type from Firebase
            database = FirebaseDatabase.getInstance();
            rootRef = database.getReference();
            rootRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    type = dataSnapshot.child(id).child("type").getValue(String.class);
                    if (type != null) { // Add this check to ensure that this won't throw a NullPointerException
                        if (type.equals("fish")) {
                            name = "fish";
                            bitmap = Utils.drawableToBitmap(getResources().getDrawable(R.drawable.ic_clown_fish_svgrepo_com));
                            path = Utils.encodeTobase64(bitmap);
                            //path = Utils.saveToInternalStorage(bitmap, name, MainActivity.this);
                        } else if (type.equals("plant")) {
                            name = "plant";
                            bitmap = Utils.drawableToBitmap(getResources().getDrawable(R.drawable.ic_plant_svgrepo_com));
                            path = Utils.encodeTobase64(bitmap);
                            //path = Utils.saveToInternalStorage(bitmap, name, MainActivity.this);
                        } else {
                            name = "hamster";
                            bitmap = Utils.drawableToBitmap(getResources().getDrawable(R.drawable.ic_hamster_svgrepo_com));
                            path = Utils.encodeTobase64(bitmap);
                            //path = Utils.saveToInternalStorage(bitmap, name, MainActivity.this);
                        }
                        petDataSource.addData(name, path, type, id);
                        petAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Li Ying", databaseError.getMessage());
                }
            });
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        SharedPreferences.Editor prefsEditor = mPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(petDataSource);
        prefsEditor.putString(KEY_DATA,json);
        prefsEditor.apply();
    }
}
