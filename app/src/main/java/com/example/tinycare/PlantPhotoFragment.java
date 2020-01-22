package com.example.tinycare;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import static android.content.Context.MODE_PRIVATE;

public class PlantPhotoFragment extends Fragment {

    View view;

    final String KEY_DATA = "data";
    final static String KEY_ENTRY_NUMBER = "entry_number";
    final String PREF_FILE = "mainsharedpref";
    SharedPreferences mPreferences;
    PetDataSource petDataSource;
    int entryNo;

    TextView plantPhotoDesc;
    FloatingActionButton plantPhotoFab;
    ProgressBar plantPhotoProgressBar;
    ImageView plantPhoto;

    String name;
    String id;

    public PlantPhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_plant_photo, container, false);

        // Find views needed
        plantPhotoDesc = view.findViewById(R.id.plantPhotoDesc);
        plantPhotoFab = view.findViewById(R.id.plantPhotoFab);
        plantPhotoProgressBar = view.findViewById(R.id.plantPhotoProgressBar);
        plantPhoto = view.findViewById(R.id.plantPhoto);

        // Load petDataSource and entryNo from shared Preferences
        mPreferences = getContext().getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        String json = mPreferences.getString(KEY_DATA, "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            petDataSource = gson.fromJson(json, PetDataSource.class);
        } else {
            petDataSource = new PetDataSource();
        }
        entryNo = mPreferences.getInt(KEY_ENTRY_NUMBER, 0);

        // Get name and id
        name = petDataSource.getName(entryNo);
        id = petDataSource.getId(entryNo);

        // Display name properly
        plantPhotoDesc.setText("Check " + name + "'s current activity.");

        // Get Database References
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference takePic = database.getReference().child(id).child("takePic");
        final DatabaseReference picUrl = database.getReference().child(id).child("picUrl");

        // Before photo is loaded at start-up
        plantPhotoProgressBar.setVisibility(View.VISIBLE);

        // Update image every time photo is taken
        picUrl.addValueEventListener(new ImEvtListener());

        // Photo Tab
        plantPhotoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePic.setValue("true");
                plantPhotoProgressBar.setVisibility(View.VISIBLE);
                plantPhoto.setVisibility(View.GONE);
            }
        });

        return view;

    }

    // create inner classes for event listeners
    // we can't use anonymous classes because we need to reference non-final vars
    private class ImEvtListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            String url = dataSnapshot.getValue(String.class);
            final ProgressBar photoProgressBar = view.findViewById(R.id.plantPhotoProgressBar);
            final ImageView plantPhoto = view.findViewById(R.id.plantPhoto);

            // If url is empty then load default image (refresh icon)
            if (url == null || url.equals("")) {
                plantPhoto.setImageResource(R.drawable.ic_refresh_black_24dp);
                plantPhoto.setVisibility(View.VISIBLE);
                photoProgressBar.setVisibility(View.GONE);
            } else {
                // Get Database References
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference httpsRef = storage.getReferenceFromUrl(url);

                GlideApp.with(getActivity())
                        .load(httpsRef)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                photoProgressBar.setVisibility(View.GONE);
                                plantPhoto.setVisibility(View.VISIBLE);
                                Toast.makeText(getActivity(), "Failed to load image. Please try again later.", Toast.LENGTH_LONG).show();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                photoProgressBar.setVisibility(View.GONE);
                                plantPhoto.setVisibility(View.VISIBLE);
                                return false;
                            }
                        })
                        .into(plantPhoto);
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e("mickey1356", "couldn't read database");
        }
    }

}
