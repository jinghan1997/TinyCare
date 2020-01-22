package com.example.tinycare;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import static android.content.Context.MODE_PRIVATE;

public class PlantMainFragment extends Fragment {

    View view;

    final String KEY_DATA = "data";
    final static String KEY_ENTRY_NUMBER = "entry_number";
    final String PREF_FILE = "mainsharedpref";
    SharedPreferences mPreferences;
    PetDataSource petDataSource;
    int entryNo;

    String name;
    Bitmap dpBitmap;
    String id;

    String waterIsLow;

    TextView plantName;
    ImageView plantDpMain;
    TextView plantMainDescText;
    Switch autoTopUpSwitch;
    TextView waterAmtText;
    ImageView waterPicture;
    View waterViewBanner;
    CardView waterCardBackground;
    Button topUpWaterButton;
    TextView prevWaterTopUpDateTimeText;

    FirebaseDatabase database;
    DatabaseReference idReference;

    public PlantMainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_plant_main, container, false);

        // Get views needed
        plantName = view.findViewById(R.id.plantName);
        plantDpMain = view.findViewById(R.id.plantDpMain);
        plantMainDescText = view.findViewById(R.id.plantMainDescText);
        autoTopUpSwitch = view.findViewById(R.id.plantAutoTopUpSwitch);
        waterCardBackground = view.findViewById(R.id.plantWaterCardBackground);
        waterViewBanner = view.findViewById(R.id.plantWaterViewBanner);
        topUpWaterButton = view.findViewById(R.id.plantTopUpWaterButton);
        waterPicture = view.findViewById(R.id.plantWaterPicture);
        waterAmtText = view.findViewById(R.id.plantWaterAmtText);
        prevWaterTopUpDateTimeText = view.findViewById(R.id.plantPrevWaterTopUpDateTimeText);

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

        // Get plant name, profile picture and id
        name = petDataSource.getName(entryNo);
        dpBitmap = petDataSource.getImage(entryNo);
        id = petDataSource.getId(entryNo);

        // Display plant name and profile picture
        plantName.setText("Hello, " + name + "!");
        plantMainDescText.setText("Check " + name + "'s food and water levels and water cleanliness.");
        plantDpMain.setImageBitmap(dpBitmap);

        // Get Firebase Realtime Database References
        database = FirebaseDatabase.getInstance();
        idReference = database.getReference().child(id);
        final DatabaseReference autoTopUp = idReference.child("autoTopUpSwitch");
        final DatabaseReference waterLow = idReference.child("waterLow");
        final DatabaseReference topUpWater = idReference.child("topUpWater");
        final DatabaseReference prevWaterTopUpDateTime = idReference.child("prevWaterTopUpDateTime");

        // Set up auto-top up switch previous value and checked/unchecked listener
        autoTopUp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(String.class).equals("true")) {
                    autoTopUpSwitch.setChecked(true);
                } else {
                    autoTopUpSwitch.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Li Ying", "Error retrieving firebase value");
            }
        });
        autoTopUpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (autoTopUpSwitch.isChecked()) {
                    autoTopUp.setValue("true");
                } else {
                    autoTopUp.setValue("false");
                }
            }
        });

        // Check water value and set image and text accordingly
        waterLow.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String waterLowValue = dataSnapshot.getValue(String.class);
                waterIsLow = waterLowValue;
                if (waterLowValue.equals("false")) {
                    waterAmtText.setText("Moist");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorFullText));
                    waterPicture.setImageResource(R.drawable.ic_hamster_water_full);
                    waterViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorMidHeader));
                    waterCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorMidCard));
                    topUpWaterButton.setBackground(getResources().getDrawable(R.drawable.custom_button_blue));
                } else if (waterLowValue.equals("true")) {
                    waterAmtText.setText("Dry");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorEmptyText));
                    waterPicture.setImageResource(R.drawable.ic_hamster_water_low);
                    waterViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorEmptyText));
                    waterCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorEmptyCard));
                    topUpWaterButton.setBackground(getResources().getDrawable(R.drawable.custom_button_red));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Li Ying", "Failed to read value.", error.toException());
            }
        });

        // Set listener for Top-Up Food button
        topUpWaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (waterIsLow.equals("true")) {
                    Toast.makeText(getActivity(), "Already watered recently!", Toast.LENGTH_LONG).show();
                } else {
                    topUpWater.setValue("true");
                    Toast.makeText(getActivity(), "Watering plant...", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Set listener to update date and time for last top-up of food
        prevWaterTopUpDateTime.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String dateTime = dataSnapshot.getValue(String.class);
                prevWaterTopUpDateTimeText.setText("Last Top-Up: " + dateTime);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Li Ying", "Failed to read value.", error.toException());
            }
        });

        return view;

    }

}
