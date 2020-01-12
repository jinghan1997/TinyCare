package com.example.tinycare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    Context context;
    LayoutInflater mInflater;
    DataSource dataSource;

    PetAdapter(Context context, DataSource dataSource){
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.dataSource = dataSource; /** this completes TODO 11.3 */
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = mInflater.inflate(R.layout.pet, viewGroup,
                false);
        return new PetViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder petViewHolder, int i) {
        petViewHolder.textViewName.setText(  dataSource.getName(i));
        petViewHolder.imageViewPet.setImageBitmap(
                dataSource.getImage(i));
    }

    @Override
    public int getItemCount() {
        return dataSource.getSize();
    }

    static class PetViewHolder extends RecyclerView.ViewHolder{
        ImageView imageViewPet;
        TextView textViewName;

        PetViewHolder(View view){
            super(view);
            imageViewPet = view.findViewById(R.id.cardViewImage);
            textViewName = view.findViewById(R.id.cardViewTextName);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), textViewName.getText(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}
