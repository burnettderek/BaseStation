package com.decibel.civilianc2.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.decibel.civilianc2.model.entities.AX25MessageRecord;
import com.decibel.civilianc2.model.entities.Station;
import com.decibel.civilianc2.model.managers.Model;
import com.example.libcivilian.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dburnett on 4/16/2018.
 */

public class StationListAdapter extends ArrayAdapter<Station> {
    public StationListAdapter(Context context, List<Station> stations){
        super(context, 0, 0, stations);
        if(typeface == null){
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/whitrabt.ttf");
        }
    }

    public interface IEventListener {
        void onStationSelected(Station station);
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent)
    {
        Station station =  getItem(pos);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.station_view, parent, false);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Station station = (Station)v.getTag();
                    onStationSelected(station);
                }
            });
        }
        convertView.setTag(station);
        TextView txtName = convertView.findViewById(R.id.txtStationName);
        TextView txtLastMessage = convertView.findViewById(R.id.txtLastMessage);
        TextView txtLastHeardOn = convertView.findViewById(R.id.txtLastHeardOn);

        txtName.setTypeface(typeface);
        if(station != null) {
            txtName.setText(station.getCallsign());
            AX25MessageRecord lastMessage = Model.getInstance().getAx25MessageManager().getLastMessage(station.getCallsign());
            if(lastMessage != null){
                txtLastMessage.setText(lastMessage.getPayload());
            } else {
                txtLastMessage.setText("No messages from this station yet.");
            }
            Date lastHeard = Model.getInstance().getStationManager().getLastUpdatedOn(station);
            txtLastHeardOn.setText(" " + dateFormat.format(lastHeard));
        }

        return convertView;
    }

    private void onStationSelected(Station station){
        for(IEventListener listener : eventListeners){
            listener.onStationSelected(station);
        }
    }

    public void addEventListener(IEventListener listener){
        eventListeners.add(listener);
    }

    public void removeEventListener(IEventListener listener){
        eventListeners.remove(listener);
    }

    private List<IEventListener> eventListeners = new ArrayList<>();
    private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss Z EEE, d MMM yyyy");
    private static Typeface typeface;
}