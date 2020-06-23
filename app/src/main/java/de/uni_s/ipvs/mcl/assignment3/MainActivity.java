package de.uni_s.ipvs.mcl.assignment3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "path";
    private FirebaseDatabase database;
    private DatabaseReference dbRef, dbRef_;
    private ArrayList<String> cities, avgTemp, temp;
    private ListView list;
    private Button btnAdd;
    private String out, newTemp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cities = new ArrayList<>();
        list = findViewById(R.id.list_item);
        btnAdd = findViewById(R.id.btnAdd);

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("location");
        dbRef.addValueEventListener(changeListener);

        if (!cities.isEmpty()){
            ArrayAdapter adapter = new ArrayAdapter<String>(this,
                    R.layout.mylist, R.id.twCity, cities);
            list.setAdapter(adapter);
        }
        list.setOnItemClickListener((new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = "location/" + (String) list.getItemAtPosition(position);
                dbRef_ = database.getReference(path);
                dbRef_.addListenerForSingleValueEvent(listener);
            }
        }));
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertBox();
            }
        });
    }

    ValueEventListener changeListener = new ValueEventListener() {

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            cities = new ArrayList<>();
            for (DataSnapshot postSnapshot: snapshot.getChildren()){
                Log.d("City", String.valueOf(postSnapshot.getKey()));
                cities.add(String.valueOf(postSnapshot.getKey()));
            }
            if (!cities.isEmpty()){
                ArrayAdapter adapter = new ArrayAdapter<String>(MainActivity.this,
                        R.layout.mylist, R.id.twCity, cities);
                list.setAdapter(adapter);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.getChildrenCount() >= 1){
                SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
                ArrayList<Date> dateArrayList = new ArrayList<>();
                Date date, maxDate = null;
                String latestDayPath = "";
                for (DataSnapshot postSnapshot: snapshot.getChildren()){
                    Log.d("Day", String.valueOf(postSnapshot.getKey()));
                    try {
                        dateArrayList.add(sdformat.parse(String.valueOf(postSnapshot.getKey())));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                maxDate = dateArrayList.get(0);
                for (Date d: dateArrayList){
                    if (d.compareTo(maxDate) > 0){
                        maxDate = d;

                    }
                }
                Log.d("MaxDate", String.valueOf(sdformat.format(maxDate)));
                Log.d("Ref", String.valueOf(snapshot.getRef()));
                Log.d("RefDate", String.valueOf(snapshot.getRef()) + "/" + String.valueOf(sdformat.format(maxDate)));
                String path = String.valueOf(snapshot.getRef()) + "/" + String.valueOf(sdformat.format(maxDate));

                Intent i = new Intent(MainActivity.this, CityActivity.class);
                i.putExtra(TAG, path);
                startActivity(i);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void insertBox(){
        LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.box_city, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("New City");
        alertDialog.setCancelable(false);



        final EditText etCity = (EditText) view.findViewById(R.id.etCity);
        final EditText etTemp = (EditText) view.findViewById(R.id.etTemp);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String newCity = null, newTemp = null;

                if (isNumeric(etTemp.getText().toString())){
                    newTemp = etTemp.getText().toString();
                }
                if (isString(etCity.getText().toString())){
                    newCity = etCity.getText().toString();

                }
                if (!newCity.isEmpty() && !newTemp.isEmpty()){
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = new Date();
                    String path_ = "location/" + newCity + "/" + String.valueOf(formatter.format(date));
                    dbRef_ = database.getReference(path_);
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

    public static boolean isString(String str){
        if (str.isEmpty() || str == ""){
            return false;
        }
        return true;
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
