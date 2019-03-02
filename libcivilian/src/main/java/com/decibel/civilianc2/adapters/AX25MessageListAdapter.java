package com.decibel.civilianc2.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.decibel.civilianc2.model.entities.AX25MessageRecord;
import com.example.libcivilian.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by dburnett on 4/16/2018.
 */

public class AX25MessageListAdapter extends ArrayAdapter<AX25MessageRecord> {
    public AX25MessageListAdapter(Context context, List<AX25MessageRecord> messages){
        super(context, 0, 0, messages);
        if(typeface == null){
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/whitrabt.ttf");
        }
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent)
    {
        AX25MessageRecord message =  getItem(pos);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.ax25message_view, parent, false);
        }
        TextView txtTimeStamp = convertView.findViewById(R.id.txtTimeStamp);
        TextView txtMessage = convertView.findViewById(R.id.txtAX25Message);


        if(message != null) {
            txtTimeStamp.setText(dateFormat.format(message.getReceivedOn()));
            txtMessage.setText(message.getPayload());
            if(message.getSupported()){
                txtMessage.setTextColor(0xff32cd32); //Lime
            } else {
                txtMessage.setTextColor(Color.RED);
            }
        }

        return convertView;
    }

    private static DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss Z EEE, d MMM yyyy");
    private static Typeface typeface;
}
