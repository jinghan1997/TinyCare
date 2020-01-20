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
        return new PetViewHolder(itemView, context, petDataSource, i);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder petViewHolder, int i) {
        petViewHolder.textViewName.setText(  petDataSource.getName(i));
        petViewHolder.imageViewPet.setImageBitmap(
                petDataSource.getImage(i));
    }

    @Override
    public int getItemCount() {
        return petDataSource.getSize();
    }

    static class PetViewHolder extends RecyclerView.ViewHolder{
        ImageView imageViewPet;
        TextView textViewName;

        final static String KEY_ENTRY_NUMBER = "entry_number";
        final String PREF_FILE = "mainsharedpref";
        SharedPreferences mPreferences;


        PetViewHolder(View view, final Context context, final PetDataSource petDataSource, final int i){
            super(view);
            imageViewPet = view.findViewById(R.id.cardViewImage);
            textViewName = view.findViewById(R.id.cardViewTextName);
            mPreferences = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (petDataSource.getType(i).equals("hamster")) {
                        Intent intent = new Intent(context, HamsterActivity.class);
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putInt(KEY_ENTRY_NUMBER, i);
                        editor.apply();
                        Toast.makeText(view.getContext(),
                                textViewName.getText().toString() + " " + i,
                                Toast.LENGTH_LONG).show();
                        context.startActivity(intent);
                    }
                }
            });
        }
    }

}
