package de.uni_s.ipvs.mcl.assignment3;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CityActivity extends AppCompatActivity {
    private static final String TAG = "path";
    private TextView twCity, twTemp, twAvgTemp;
    private Button btnAdd;
    private ArrayList<Float> temp;
    private FirebaseDatabase database;
    private DatabaseReference dbRef, dbRef_;
    private String path;
    private Double currTemp, avg, sum;
    private int count;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        count = 0;
        sum = 0.0;

        twCity = findViewById(R.id.twCity);
        twTemp = findViewById(R.id.twTemp);
        twAvgTemp = findViewById(R.id.twAvgTemp);
        btnAdd = findViewById(R.id.btnAdd);


        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            path = extras.getString(TAG);
        }
        else{
            finish();
        }

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReferenceFromUrl(path);
        dbRef.addChildEventListener(listener);

        Log.d("Index", String.valueOf(path.indexOf("/", 45)));
//        String path_ = path + "/" + String.valueOf(System.currentTimeMillis());
//        dbRef_ = database.getReferenceFromUrl(path_);
//        dbRef_.setValue("1");
        String cityName = path.substring(50, path.indexOf("/", 50));
        twCity.setText(cityName);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertBox();
            }
        });
    }

    ChildEventListener listener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            Log.d("RefCityAct", String.valueOf(snapshot.getRef()));
            Log.d("Key", snapshot.getKey());
            Log.d("Value", String.valueOf(snapshot.getValue()));
            sum += Double.parseDouble(String.valueOf(snapshot.getValue()));
            count++;
            avg = sum/count;
            currTemp = Double.parseDouble(String.valueOf(snapshot.getValue()));
            twTemp.setText(String.valueOf(currTemp) + "°C");
            DecimalFormat df = new DecimalFormat("#.##");
            twAvgTemp.setText(df.format(avg) + "°C");
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void insertBox(){
        LayoutInflater inflater = (LayoutInflater) CityActivity.this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.box_, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(CityActivity.this).create();
        alertDialog.setTitle("New Temperature");
        alertDialog.setCancelable(false);



        final EditText etTemp = (EditText) view.findViewById(R.id.etTemp);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String newTemp;
                if (isNumeric(etTemp.getText().toString())){
                    newTemp = etTemp.getText().toString();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = new Date();
                    Log.d("Time", String.valueOf(formatter.format(date)));
                    String path_ = path.substring(0, path.indexOf("/", 50) + 1) + String.valueOf(formatter.format(date));
                    Log.d("Test day", path_);

                    dbRef_ = database.getReferenceFromUrl(path_);
                    dbRef_.push().setValue(newTemp);
                }

            }
        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });


        alertDialog.setView(view);
        alertDialog.show();

    }

    public static boolean isNumeric(String str) {
        try {
            Float.parseFloat(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
}
