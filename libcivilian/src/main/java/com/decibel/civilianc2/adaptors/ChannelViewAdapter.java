package com.decibel.civilianc2.adaptors;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.decibel.civilianc2.radios.Channel;
import com.example.libcivilian.R;

import java.util.List;

/**
 * Created by dburnett on 2/13/2018.
 */

public class ChannelViewAdapter extends ArrayAdapter<Channel> {
    public ChannelViewAdapter(Context context, List<Channel> channels){
        super(context, 0, 0, channels);
        if(typeface == null){
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/whitrabt.ttf");
        }
    }


    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent)
    {
        Channel channel = null;
        if(pos > 0){
            channel = getItem(pos - 1);
        }

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.channel_view, parent, false);
        }
        TextView txtName = convertView.findViewById(R.id.txtChannelName);
        TextView txtDesc = convertView.findViewById(R.id.txtChannelDesc);
        TextView txtFreq = convertView.findViewById(R.id.txtChannelFreq);
        TextView lblFrequency = convertView.findViewById(R.id.lblFrequency);

        txtName.setTypeface(typeface);
        if(channel != null) {
            txtName.setText(channel.getName());
            txtDesc.setText(channel.getDescription());
            txtDesc.setVisibility(View.VISIBLE);
            txtFreq.setVisibility(View.VISIBLE);
            lblFrequency.setVisibility(View.VISIBLE);

            if(Math.abs(channel.getTxFreq() - (channel.getRxFreq() - 0.600)) < 0.001)
            {
                txtFreq.setText(channel.getRxFreq() + "-");
            } else if (Math.abs(channel.getTxFreq() - (channel.getRxFreq() + 0.600)) < 0.001){
                txtFreq.setText(channel.getRxFreq() + "+");
            } else {
                txtFreq.setText(new String() + channel.getRxFreq());
            }
        } else {
            txtName.setVisibility(View.INVISIBLE);
            txtDesc.setVisibility(View.INVISIBLE);
            txtFreq.setVisibility(View.INVISIBLE);
            lblFrequency.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int pos, View convertView, ViewGroup parent){
        Channel channel = null;
        if(pos > 0){
            channel = getItem(pos - 1);
        }
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.channel_view, parent, false);
        }
        TextView txtName = convertView.findViewById(R.id.txtChannelName);
        TextView txtDesc = convertView.findViewById(R.id.txtChannelDesc);
        TextView txtFreq = convertView.findViewById(R.id.txtChannelFreq);
        TextView lblFrequency = convertView.findViewById(R.id.lblFrequency);


        if(channel != null) {
            txtName.setTypeface(typeface);
            txtName.setText(channel.getName());
            txtDesc.setText(channel.getDescription());
            txtDesc.setVisibility(View.VISIBLE);
            txtFreq.setVisibility(View.VISIBLE);
            lblFrequency.setVisibility(View.VISIBLE);

            if(Math.abs(channel.getTxFreq() - (channel.getRxFreq() - 0.600)) < 0.001)
            {
                txtFreq.setText(channel.getRxFreq() + "-");
            } else if (Math.abs(channel.getTxFreq() - (channel.getRxFreq() + 0.600)) < 0.001){
                txtFreq.setText(channel.getRxFreq() + "+");
            } else {
                txtFreq.setText(new String() + channel.getRxFreq());
            }
        } else {
            txtName.setTypeface(Typeface.DEFAULT);
            txtName.setVisibility(View.VISIBLE);
            txtName.setText("Cancel");
            txtDesc.setVisibility(View.GONE);
            txtFreq.setVisibility(View.GONE);
            lblFrequency.setVisibility(View.GONE);
        }
        return convertView;
    }

    private static Typeface typeface;
}
