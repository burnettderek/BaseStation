package com.decibel.civilianc2.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.decibel.civilianc2.adapters.StationListAdapter;
import com.decibel.civilianc2.model.entities.Station;
import com.decibel.civilianc2.model.managers.Model;
import com.example.libcivilian.R;

import java.util.List;

public class ViewStations extends Activity implements StationListAdapter.IEventListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stations);
        List<Station> stations = Model.getInstance().getStationManager().getAll();

        StationListAdapter adapter = new StationListAdapter(this, stations);
        ListView stationList = findViewById(R.id.listStations);
        stationList.setAdapter(adapter);
        adapter.addEventListener(this);
    }

    @Override
    public void onStationSelected(Station station) {
        Intent intent = new Intent(this, ViewStation.class);
        intent.putExtra(ViewStation.CallSignParameter, station.getCallsign());
        startActivity(intent);
    }
}
