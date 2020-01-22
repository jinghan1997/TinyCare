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

public class FishMainFragment extends Fragment {

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

    String foodIsHigh;
    String waterIsLow;

    TextView fishName;
    ImageView fishDpMain;
    TextView fishMainDescText;
    Switch autoTopUpSwitch;
    TextView foodAmtText;
    TextView waterAmtText;
    TextView cleanlinessAmtText;
    ImageView foodPicture;
    ImageView waterPicture;
    ImageView cleanlinessPicture;
    View foodViewBanner;
    View waterViewBanner;
    View cleanlinessViewBanner;
    CardView foodCardBackground;
    CardView waterCardBackground;
    CardView cleanlinessCardBackground;
    Button topUpFoodButton;
    Button topUpWaterButton;
    TextView prevFoodTopUpDateTimeText;

    FirebaseDatabase database;
    DatabaseReference idReference;

    public FishMainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_fish_main, container, false);

        // Get views needed
        fishName = view.findViewById(R.id.fishName);
        fishDpMain = view.findViewById(R.id.fishDpMain);
        fishMainDescText = view.findViewById(R.id.fishMainDescText);
        autoTopUpSwitch = view.findViewById(R.id.fishAutoTopUpSwitch);
        foodCardBackground = view.findViewById(R.id.fishFoodCardBackground);
        waterCardBackground = view.findViewById(R.id.fishWaterCardBackground);
        cleanlinessCardBackground = view.findViewById(R.id.fishCleanlinessCardBackground);
        foodViewBanner = view.findViewById(R.id.fishFoodViewBanner);
        waterViewBanner = view.findViewById(R.id.fishWaterViewBanner);
        cleanlinessViewBanner = view.findViewById(R.id.fishCleanlinessViewBanner);
        topUpFoodButton = view.findViewById(R.id.fishTopUpFoodButton);
        topUpWaterButton = view.findViewById(R.id.fishTopUpWaterButton);
        foodPicture = view.findViewById(R.id.fishFoodPicture);
        waterPicture = view.findViewById(R.id.fishWaterPicture);
        cleanlinessPicture = view.findViewById(R.id.fishCleanlinessPicture);
        foodAmtText = view.findViewById(R.id.fishFoodAmtText);
        waterAmtText = view.findViewById(R.id.fishWaterAmtText);
        cleanlinessAmtText = view.findViewById(R.id.fishCleanlinessAmtText);
        prevFoodTopUpDateTimeText = view.findViewById(R.id.fishPrevFoodTopUpDateTimeText);

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

        // Get fish name, profile picture and id
        name = petDataSource.getName(entryNo);
        dpBitmap = petDataSource.getImage(entryNo);
        id = petDataSource.getId(entryNo);

        // Display fish name and profile picture
        fishName.setText("Hello, " + name + "!");
        fishMainDescText.setText("Check " + name + "'s food and water levels and water cleanliness.");
        fishDpMain.setImageBitmap(dpBitmap);

        // Get Firebase Realtime Database References
        database = FirebaseDatabase.getInstance();
        idReference = database.getReference().child(id);
        final DatabaseReference autoTopUp = idReference.child("autoTopUpSwitch");
        final DatabaseReference withinHours = idReference.child("withinHours");
        final DatabaseReference waterLow = idReference.child("waterLow");
        final DatabaseReference waterDirty = idReference.child("waterDirty");
        final DatabaseReference topUpFood = idReference.child("topUpFood");
        final DatabaseReference topUpWater = idReference.child("topUpWater");
        final DatabaseReference prevFoodTopUpDateTime = idReference.child("prevFoodTopUpDateTime");

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

        // Check food value and set image and text accordingly
        withinHours.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String withinHoursValue = dataSnapshot.getValue(String.class);
                foodIsHigh = withinHoursValue;
                if (withinHoursValue.equals("true")) {
                    foodAmtText.setText("Yes");
                    foodAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorFullText));
                    foodPicture.setImageResource(R.drawable.ic_hamster_food_full);
                    foodViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorMidHeader));
                    foodCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorMidCard));
                    topUpFoodButton.setBackground(getResources().getDrawable(R.drawable.custom_button_blue));
                } else if (withinHoursValue.equals("false")) {
                    foodAmtText.setText("No");
                    foodAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorEmptyText));
                    foodPicture.setImageResource(R.drawable.ic_hamster_food_empty);
                    foodViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorEmptyText));
                    foodCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorEmptyCard));
                    topUpFoodButton.setBackground(getResources().getDrawable(R.drawable.custom_button_red));
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Li Ying", "Failed to read value.", error.toException());
            }
        });

        // Set listener for Top-Up Food button
        topUpFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (foodIsHigh.equals("true")) {
                    Toast.makeText(getActivity(), "Already fed recently!", Toast.LENGTH_LONG).show();
                } else {
                    topUpFood.setValue("true");
                    Toast.makeText(getActivity(), "Food is added!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Set listener to update date and time for last top-up of food
        prevFoodTopUpDateTime.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String dateTime = dataSnapshot.getValue(String.class);
                prevFoodTopUpDateTimeText.setText("Last Top-Up: " + dateTime);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Li Ying", "Failed to read value.", error.toException());
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
                    waterAmtText.setText("Sufficient");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorFullText));
                    waterPicture.setImageResource(R.drawable.ic_hamster_water_full);
                    waterViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorMidHeader));
                    waterCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorMidCard));
                    topUpWaterButton.setBackground(getResources().getDrawable(R.drawable.custom_button_blue));
                } else if (waterLowValue.equals("true")) {
                    waterAmtText.setText("Low");
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

        // Set listener for Top-Up Water button
        topUpWaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (waterIsLow.equals("false")) {
                    Toast.makeText(getActivity(), "Water is sufficient!", Toast.LENGTH_LONG).show();
                } else {
                    topUpWater.setValue("true");
                    Toast.makeText(getActivity(), "Water is added!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Check water cleanliness and set image and text accordingly
        waterDirty.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String waterDirtyValue = dataSnapshot.getValue(String.class);
                if (waterDirtyValue.equals("false")) {
                    cleanlinessAmtText.setText("Clean");
                    cleanlinessAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorFullText));
                    cleanlinessPicture.setImageResource(R.drawable.ic_fish_tank);
                    cleanlinessViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorMidHeader));
                    cleanlinessCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorMidCard));
                } else if (waterDirtyValue.equals("true")) {
                    cleanlinessAmtText.setText("Dirty");
                    cleanlinessAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorEmptyText));
                    cleanlinessPicture.setImageResource(R.drawable.ic_fish_tank_dirty);
                    cleanlinessViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorEmptyText));
                    cleanlinessCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorEmptyCard));
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Li Ying", "Failed to read value.", error.toException());
            }
        });

        return view;
    }

}
