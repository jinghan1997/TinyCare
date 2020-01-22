package com.example.tinycare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        petViewHolder.textViewName.setText(  petDataSource.getName(i));
        petViewHolder.imageViewPet.setImageBitmap(
                petDataSource.getImage(i));
        petViewHolder.i = i;
        petViewHolder.petDataSource = petDataSource;
    }

    @Override
    public int getItemCount() {
        return petDataSource.getSize();
    }

    static class PetViewHolder extends RecyclerView.ViewHolder{
        ImageView imageViewPet;
        TextView textViewName;
        Context context;
        int i;
        PetDataSource petDataSource;

        final static String KEY_ENTRY_NUMBER = "entry_number";
        final String PREF_FILE = "mainsharedpref";
        SharedPreferences mPreferences;


        PetViewHolder(View view, Context context){
            super(view);
            imageViewPet = view.findViewById(R.id.cardViewImage);
            textViewName = view.findViewById(R.id.cardViewTextName);
            mPreferences = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
            this.context = context;

            view.setOnClickListener(new petOnClickListener());
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
