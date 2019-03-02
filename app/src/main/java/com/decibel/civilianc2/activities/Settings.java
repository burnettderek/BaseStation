package com.decibel.civilianc2.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.decibel.civilianc2.R;
import com.decibel.civilianc2.adaptors.SSIDViewAdapter;
import com.decibel.civilianc2.model.dataaccess.UserSettings;
import com.decibel.civilianc2.model.entities.Position;
import com.decibel.civilianc2.model.managers.Model;

import java.util.ArrayList;
import java.util.List;

public class Settings extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageView back = findViewById(R.id.btnBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDatabase(); finish();
            }
        });

        callsignEdit = findViewById(R.id.callSignEdit);
        String cs = Model.getInstance().getUserSettings().getSetting(UserSettings.CallSign);
        callsignEdit.setText(cs);


        final Spinner spinner = findViewById(R.id.ssidSpinner);

        List<String> ssids = new ArrayList<>();
        ssids.add("None (Primary Station)");
        for(int i = 1; i < 10; i++){
            ssids.add(Integer.toString(i));
        }
        spinner.setAdapter(new SSIDViewAdapter(this, ssids));

        String ssid = Model.getInstance().getUserSettings().getSetting(UserSettings.SSID);
        if(ssid != null && ssid.length() > 0)
            spinner.setSelection(Integer.parseInt(ssid));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(spinner.getSelectedItemPosition() == 0){
                    Model.getInstance().getUserSettings().setSetting(UserSettings.SSID, "");
                } else {
                    Model.getInstance().getUserSettings().setSetting(UserSettings.SSID, (String) spinner.getSelectedItem());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        latitudeEdit = findViewById(R.id.latitudeEdit);
        longitudeEdit = findViewById(R.id.logitudeEdit);

        String location = Model.getInstance().getUserSettings().getSetting(UserSettings.Location);
        if(location != null && location.length() > 0) {
            String[] latlon = location.split(",");
            latitudeEdit.setText(latlon[0]);
            longitudeEdit.setText(latlon[1]);
        }

        commentEdit = findViewById(R.id.commentEdit);
        String comment = Model.getInstance().getUserSettings().getSetting(UserSettings.APRSCommentField);
        commentEdit.setText(comment);
    }


    public void updateDatabase() {
        String lat  = latitudeEdit.getText().toString();
        String lon  = longitudeEdit.getText().toString();
        if(!isNullOrEmpty(lat) && !isNullOrEmpty(lon)) {
            Position position = new Position(Double.parseDouble(lat), Double.parseDouble(lon));
            Model.getInstance().getUserSettings().setSetting(UserSettings.Location, position);
        }

        String comment = commentEdit.getText().toString();
        Model.getInstance().getUserSettings().setSetting(UserSettings.APRSCommentField, comment);

        Model.getInstance().getUserSettings().setSetting(UserSettings.CallSign, callsignEdit.getText().toString());
    }

    private boolean isNullOrEmpty(String str){
        return (str == null || str.isEmpty());
    }

    EditText latitudeEdit;
    EditText longitudeEdit;
    EditText commentEdit;
    EditText callsignEdit;
}
