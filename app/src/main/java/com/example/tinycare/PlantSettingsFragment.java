package com.example.tinycare;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class PlantSettingsFragment extends Fragment {

    View view;

    final String KEY_DATA = "data";
    final static String KEY_ENTRY_NUMBER = "entry_number";
    final String PREF_FILE = "mainsharedpref";
    SharedPreferences mPreferences;
    PetDataSource petDataSource;
    int entryNo;

    public static final int PICK_IMAGE_REQUEST = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;

    FloatingActionButton chooseImage;
    FloatingActionButton saveChanges;
    EditText editName;
    ImageView plantDpSettings;

    String name;
    Bitmap dpBitmap;
    String id;

    String url;
    Bitmap newImageBitmap;

    public PlantSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_plant_settings, container, false);

        // Find views needed
        editName = view.findViewById(R.id.plantEditName);
        chooseImage = view.findViewById(R.id.plantChooseImage);
        plantDpSettings = view.findViewById(R.id.plantDpSettings);
        saveChanges = view.findViewById(R.id.plantSaveChanges);

        // Load petDataSource and entryNo from shared Preferences
        mPreferences = getContext().getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        String json = mPreferences.getString(KEY_DATA, "");
        if( !json.isEmpty() ){
            Gson gson = new Gson();
            petDataSource = gson.fromJson(json, PetDataSource.class);
        } else {
            petDataSource = new PetDataSource();
        }
        entryNo = mPreferences.getInt(KEY_ENTRY_NUMBER, 0);

        // Get plant name, profile picture and id
        name = petDataSource.getName(entryNo);
        dpBitmap = petDataSource.getImage(entryNo);
        id = petDataSource.getId(entryNo);

        // Display plant name and profile picture
        editName.setText(name);
        plantDpSettings.setImageBitmap(dpBitmap);
        newImageBitmap = dpBitmap;

        // Firebase references to retrieve PlantCare photo
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference picUrl = database.getReference().child(id).child("picUrl");
        picUrl.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                url = dataSnapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i("Li Ying", "Failed to read value.", error.toException());
            }
        });

        // Choose image from 4 options - Default, Gallery, Camera, or FishCare Photo
        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] choices = {"Default Picture",
                        "Choose from Gallery",
                        "Take a Photo", "Latest FishCare Photo"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Upload Image");
                builder.setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                        if (which == 0) {
                            // Select default image
                            plantDpSettings.setImageResource(R.drawable.ic_clown_fish_svgrepo_com);
                            newImageBitmap = Utils.drawableToBitmap(getResources().getDrawable(R.drawable.ic_clown_fish_svgrepo_com));
                        } else if (which == 1) {
                            // Select image from gallery
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent, PICK_IMAGE_REQUEST);
                        } else if (which == 2) {
                            // Take photo using phone camera
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                            }
                        } else if (which == 3) {
                            // Ensure that a photo exists in FishCare
                            if (url == null || url.equals("")) {
                                Toast.makeText(getContext(),
                                        "FishCare photo does not exist",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                // Choose latest photo of plant
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference httpsRef = storage.getReferenceFromUrl(url);
                                try {
                                    final File localFile = File.createTempFile("images", "jpg");
                                    httpsRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                            plantDpSettings.setImageBitmap(bitmap);
                                            newImageBitmap = bitmap;
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                        }
                                    });
                                } catch (IOException e) {
                                    Log.e("Li Ying", "Failed to retrieve image from firebase storage");
                                }
                            }
                        }
                    }
                });
                builder.show();
            }
        });

        // Confirm changes button
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save name to petDataSource
                petDataSource.setName(entryNo, editName.getText().toString());
                // Save image path to petDataSource (only if changed)
                if (newImageBitmap != dpBitmap) {
                    String path = Utils.encodeTobase64(newImageBitmap);
                    //String path = Utils.saveToInternalStorage(newImageBitmap, name, getContext());
                    petDataSource.setPath(entryNo, path);
                }
                Toast.makeText(view.getContext(), "Settings saved!", Toast.LENGTH_LONG).show();
            }
        });

        return view;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null ) {
            Bitmap thumbnail = data.getParcelableExtra("data");
            Uri fullPhotoUri = data.getData();
            // bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), fullPhotoUri);
            plantDpSettings.setImageURI(fullPhotoUri);
            newImageBitmap = ((BitmapDrawable)plantDpSettings.getDrawable()).getBitmap();
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            plantDpSettings.setImageBitmap(imageBitmap);
            newImageBitmap = imageBitmap;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor prefsEditor = mPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(petDataSource);
        prefsEditor.putString(KEY_DATA,json);
        prefsEditor.apply();
    }

}
