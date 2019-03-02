package com.decibel.civilianc2.adaptors;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.decibel.civilianc2.R;
import com.decibel.civilianc2.radios.Channel;

import java.util.List;

/**
 * Created by dburnett on 3/12/2018.
 */

public class SSIDViewAdapter extends ArrayAdapter<String> {
    public SSIDViewAdapter(Context context, List<String> channels){
        super(context, 0, 0, channels);
    }




    @Override
    public View getView(int pos, View convertView, ViewGroup parent)
    {

        String ssid = getItem(pos);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ssid_view, parent, false);
        }
        TextView txtSSID = convertView.findViewById(R.id.txtSSID);

        txtSSID.setText(ssid);
        return convertView;
    }

    @Override
    public View getDropDownView(int pos, View convertView, ViewGroup parent){
        return getView(pos, convertView, parent);
    }

}
