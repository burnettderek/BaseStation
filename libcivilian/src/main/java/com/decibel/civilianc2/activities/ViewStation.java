package com.decibel.civilianc2.activities;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.decibel.civilianc2.adapters.AX25MessageListAdapter;
import com.decibel.civilianc2.model.entities.AX25MessageRecord;
import com.decibel.civilianc2.model.entities.Station;
import com.decibel.civilianc2.model.managers.Model;
import com.decibel.civilianc2.protocols.ax25.aprs.TextMessage;
import com.example.libcivilian.R;

import java.util.List;

public class ViewStation extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_station);
        String stationCallsign = getIntent().getStringExtra(CallSignParameter);
        TextView txtCallSign = findViewById(R.id.txtCallsign);
        ListView messages = findViewById(R.id.listMessages);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/whitrabt.ttf");

        txtCallSign.setTypeface(typeface);
        txtCallSign.setText(stationCallsign);

        List<AX25MessageRecord> stationMessages = Model.getInstance().getAx25MessageManager().getAllMessagesForStation(stationCallsign);
        messages.setAdapter(new AX25MessageListAdapter(this, stationMessages));

    }

    public final static String CallSignParameter = "Station.Callsign";
}
