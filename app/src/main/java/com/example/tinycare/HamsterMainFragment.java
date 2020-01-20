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

public class HamsterMainFragment extends Fragment {

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

    Bitmap imageBitmap;
    String foodLevel;
    String waterLevel;

    TextView hamsterName;
    ImageView hamsterDpMain;
    TextView hamsterMainDescText;
    Switch autoTopUpSwitch;
    TextView foodAmtText;
    TextView waterAmtText;
    ImageView foodPicture;
    ImageView waterPicture;
    View foodViewBanner;
    View waterViewBanner;
    CardView foodCardBackground;
    CardView waterCardBackground;
    Button topUpFoodButton;
    Button topUpWaterButton;
    TextView prevFoodTopUpDateTimeText;
    TextView prevWaterTopUpDateTimeText;

    FirebaseDatabase database;
    DatabaseReference idReference;

    public HamsterMainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_hamster_main, container, false);

        // Get views needed
        hamsterName = view.findViewById(R.id.hamsterName);
        hamsterDpMain = view.findViewById(R.id.hamsterDpMain);
        hamsterMainDescText = view.findViewById(R.id.hamsterMainDescText);
        autoTopUpSwitch = view.findViewById(R.id.hamsterAutoTopUpSwitch);
        foodAmtText = view.findViewById(R.id.hamsterFoodAmtText);
        waterAmtText = view.findViewById(R.id.hamsterWaterAmtText);
        foodPicture = view.findViewById(R.id.hamsterFoodPicture);
        waterPicture = view.findViewById(R.id.hamsterWaterPicture);
        foodViewBanner = view.findViewById(R.id.hamsterFoodViewBanner);
        waterViewBanner = view.findViewById(R.id.hamsterWaterViewBanner);
        foodCardBackground = view.findViewById(R.id.hamsterFoodCardBackground);
        waterCardBackground = view.findViewById(R.id.hamsterWaterCardBackground);
        topUpFoodButton = view.findViewById(R.id.hamsterTopUpFoodButton);
        topUpWaterButton = view.findViewById(R.id.hamsterTopUpWaterButton);
        prevFoodTopUpDateTimeText = view.findViewById(R.id.hamsterPrevFoodTopUpDateTimeText);
        prevWaterTopUpDateTimeText = view.findViewById(R.id.hamsterPrevWaterTopUpDateTimeText);

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

        // Get hamster name, profile picture and id
        name = petDataSource.getName(entryNo);
        dpBitmap = petDataSource.getImage(entryNo);
        id = petDataSource.getId(entryNo);

        // Display hamster name and profile picture
        hamsterName.setText("Hello, " + name + "!");
        hamsterMainDescText.setText("Check " + name + "'s food and water levels.");
        hamsterDpMain.setImageBitmap(dpBitmap);

        // Get Firebase Realtime Database References
        database = FirebaseDatabase.getInstance();
        idReference = database.getReference().child(id);
        final DatabaseReference autoTopUp = idReference.child("autoTopUpSwitch");
        final DatabaseReference foodAmt = idReference.child("foodAmt");
        final DatabaseReference waterAmt = idReference.child("waterAmt");
        final DatabaseReference topUpFood = idReference.child("topUpFood");
        final DatabaseReference topUpWater = idReference.child("topUpWater");
        final DatabaseReference prevFoodTopUpDateTime = idReference.child("prevFoodTopUpDateTime");
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

        // Check food value and set image and text accordingly
        foodAmt.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String foodValue = dataSnapshot.getValue(String.class);
                foodLevel = foodValue;
                if (foodValue.equals("full")) {
                    foodAmtText.setText("Full");
                    foodAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorFullText));
                    foodPicture.setImageResource(R.drawable.ic_hamster_food_full);
                    foodViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorFullHeader));
                    foodCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorFullCard));
                    topUpFoodButton.setBackground(getResources().getDrawable(R.drawable.custom_button_grey));
                } else if (foodValue.equals("mid")) {
                    foodAmtText.setText("Mid");
                    foodAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorFullText));
                    foodPicture.setImageResource(R.drawable.ic_hamster_food_mid);
                    foodViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorMidHeader));
                    foodCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorMidCard));
                    topUpFoodButton.setBackground(getResources().getDrawable(R.drawable.custom_button_blue));
                } else if (foodValue.equals("low")) {
                    foodAmtText.setText("Low");
                    foodAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorEmptyText));
                    foodPicture.setImageResource(R.drawable.ic_hamster_food_low);
                    foodViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorEmptyText));
                    foodCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorEmptyCard));
                    topUpFoodButton.setBackground(getResources().getDrawable(R.drawable.custom_button_red));
                } else if (foodValue.equals("empty")) {
                    foodAmtText.setText("Empty");
                    foodAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorEmptyText));
                    foodPicture.setImageResource(R.drawable.ic_hamster_food_empty);
                    foodViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorEmptyText));
                    foodCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorEmptyCard));
                    topUpFoodButton.setBackground(getResources().getDrawable(R.drawable.custom_button_red));
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("jinghan", "Failed to read value.", error.toException());
            }
        });

        // Set listener for Top-Up Food button
        topUpFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (foodLevel.equals("full")) {
                    Toast.makeText(getActivity(), "Food is full!", Toast.LENGTH_LONG).show();
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
                Log.e("jinghan", "Failed to read value.", error.toException());
            }
        });

        // Check water value and set image and text accordingly
        waterAmt.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String waterValue = dataSnapshot.getValue(String.class);
                waterLevel = waterValue;
                if (waterValue.equals("full")) {
                    waterAmtText.setText("Full");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorFullText));
                    waterPicture.setImageResource(R.drawable.ic_hamster_water_full);
                    waterViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorFullHeader));
                    waterCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorFullCard));
                    topUpWaterButton.setBackground(getResources().getDrawable(R.drawable.custom_button_grey));
                } else if (waterValue.equals("mid")) {
                    waterAmtText.setText("Mid");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorFullText));
                    waterPicture.setImageResource(R.drawable.ic_hamster_water_mid);
                    waterViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorMidHeader));
                    waterCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorMidCard));
                    topUpWaterButton.setBackground(getResources().getDrawable(R.drawable.custom_button_blue));
                } else if (waterValue.equals("low")) {
                    waterAmtText.setText("Low");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorEmptyText));
                    waterPicture.setImageResource(R.drawable.ic_hamster_water_low);
                    waterViewBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorEmptyText));
                    waterCardBackground.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorEmptyCard));
                    topUpWaterButton.setBackground(getResources().getDrawable(R.drawable.custom_button_red));
                } else if (waterValue.equals("empty")) {
                    waterAmtText.setText("Empty");
                    waterAmtText.setTextColor(ContextCompat.getColor(getContext(), R.color.colorEmptyText));
                    waterPicture.setImageResource(R.drawable.ic_hamster_water_empty);
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
                if (waterLevel.equals("full")) {
                    Toast.makeText(getActivity(), "Water is full!", Toast.LENGTH_LONG).show();
                } else {
                    topUpWater.setValue("true");
                    Toast.makeText(getActivity(), "Water is added!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Set listener to update date and time for last top-up of water
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
                Log.i("Li Ying", "Failed to read value.", error.toException());
            }
        });

        return view;
    }
}
