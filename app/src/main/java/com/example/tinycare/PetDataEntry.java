package com.example.tinycare;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class PetDataEntry extends AppCompatActivity {

    final String KEY_DATA = "data";
    final String PREF_FILE = "mainsharedpref";
    SharedPreferences mPreferences;
    PetDataSource petDataSource;

    EditText editTextIdEntry;
    Button buttonOK;
    final static String KEY_ID = "Id";
    String idEntered;
    Boolean idExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_entry);

        // Get references to widgets
        editTextIdEntry = findViewById(R.id.editTextIdEntry);
        buttonOK = findViewById(R.id.buttonOK);

        // Load the Json string from shared Preferences
        mPreferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        String json = mPreferences.getString(KEY_DATA, "");
        if( !json.isEmpty() ){
            Gson gson = new Gson();
            petDataSource = gson.fromJson(json, PetDataSource.class);
        }

        // Set up OK button to return to MainActivy
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idEntered = editTextIdEntry.getText().toString();
                // Get Firebase Realtime Database References
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference rootRef = database.getReference();

                // Check if ID exists
                rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.child(idEntered).exists()) {
                            // Check if ID is already added
                            if (petDataSource != null && petDataSource.idAlreadyAdded(idEntered)) {
                                Toast.makeText(PetDataEntry.this,
                                        "ID is already added", Toast.LENGTH_LONG).show();
                            } else {
                                idExists = true;
                                int resultCode = Activity.RESULT_OK;
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra(KEY_ID, idEntered);
                                setResult(resultCode, resultIntent);
                                finish();
                            }
                        } else {
                            Toast.makeText(PetDataEntry.this,
                                    "ID does not exist", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i("ERROR", databaseError.getMessage());
                    }
                });
            }
        });

    }

}
