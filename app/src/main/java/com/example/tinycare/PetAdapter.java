package com.example.tinycare;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    Context context;
    LayoutInflater mInflater;
    PetDataSource petDataSource;


    PetAdapter(Context context, PetDataSource petDataSource){
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.petDataSource = petDataSource;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = mInflater.inflate(R.layout.pet, viewGroup,
                false);
        return new PetViewHolder(itemView, context);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder petViewHolder, int i) {
        String petName = petDataSource.getName(i);
        String type = petDataSource.getType(i);

        petViewHolder.textViewName.setText(petName.substring(0, 1).toUpperCase() + petName.substring(1));
        petViewHolder.imageViewPet.setImageBitmap(petDataSource.getImage(i));
        petViewHolder.petType.setText("Type: " + petDataSource.getType(i));
        petViewHolder.petHardwareId.setText("Hardware ID: " + petDataSource.getId(i));


        // Set Date Added
        Date date = petDataSource.getDate(i);
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
            String strDate = formatter.format(date);
            petViewHolder.petDateAdded.setText("Date Added: " + strDate);
        }

        // Set background color for each pet
        if (type.equals("hamster")) {
            petViewHolder.petCard.setCardBackgroundColor(context.getResources().getColor(R.color.colorHamsterMainCard));
        } else if (type.equals("fish")) {
            petViewHolder.petCard.setCardBackgroundColor(context.getResources().getColor(R.color.colorFishMainCard));
        } else if (type.equals("plant")) {
            petViewHolder.petCard.setCardBackgroundColor(context.getResources().getColor(R.color.colorPlantMainCard));
        }

        // Variables needed for view.onClickListener
        petViewHolder.i = i;
        petViewHolder.petDataSource = petDataSource;
    }

    @Override
    public int getItemCount() {
        return petDataSource.getSize();
    }

    class PetViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPet;
        TextView textViewName;
        TextView petType;
        TextView petHardwareId;
        TextView petDateAdded;
        FloatingActionButton deleteFab;
        Context context;
        int i;
        PetDataSource petDataSource;
        CardView petCard;

        final static String KEY_ENTRY_NUMBER = "entry_number";
        final String PREF_FILE = "mainsharedpref";
        SharedPreferences mPreferences;


        PetViewHolder(View view, Context context){
            super(view);
            imageViewPet = view.findViewById(R.id.cardViewImage);
            textViewName = view.findViewById(R.id.cardViewTextName);
            petType = view.findViewById(R.id.petType);
            petHardwareId = view.findViewById(R.id.petHardwareId);
            petDateAdded = view.findViewById(R.id.petDateAdded);
            deleteFab = view.findViewById(R.id.deleteFab);
            petCard = view.findViewById(R.id.petCard);
            mPreferences = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
            this.context = context;

            // Set listener to enter pet activity when pet view is clicked
            view.setOnClickListener(new petOnClickListener());

            // Set listener to delete pet when delete fab is clicked
            deleteFab.setOnClickListener(new deleteOnClickListener());
        }

        // create inner classes for event listeners
        // we can't use anonymous classes because we need to reference non-final vars
        private class deleteOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Do you really want to delete pet?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                PetAdapter.this.petDataSource.removeData(getAdapterPosition());
                                PetAdapter.this.notifyDataSetChanged();
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        }

        // create inner classes for event listeners
        // we can't use anonymous classes because we need to reference non-final vars
        private class petOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View view) {
                // Check for internet connection to prevent crashing later
                if (!Utils.isNetworkAvailable(context)) {
                    Toast.makeText(context,
                            "Not connected to Internet", Toast.LENGTH_LONG).show();
                } else {
                    if (petDataSource.getType(i).equals("hamster")) {
                        Intent intent = new Intent(context, HamsterActivity.class);
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putInt(KEY_ENTRY_NUMBER, i);
                        editor.apply();
                        context.startActivity(intent);
                    } else if (petDataSource.getType(i).equals("fish")) {
                        Intent intent = new Intent(context, FishActivity.class);
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putInt(KEY_ENTRY_NUMBER, i);
                        editor.apply();
                        context.startActivity(intent);
                    } else if (petDataSource.getType(i).equals("plant")) {
                        Intent intent = new Intent(context, PlantActivity.class);
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putInt(KEY_ENTRY_NUMBER, i);
                        editor.apply();
                        context.startActivity(intent);
                    }
                }
            }

        }
    }

}
