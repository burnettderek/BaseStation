package com.decibel.civilianc2.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.decibel.civilianc2.R;
import com.decibel.civilianc2.model.entities.MessageRecord;

import java.util.List;

/**
 * Created by dburnett on 3/26/2018.
 */

public class MessageViewAdapter extends ArrayAdapter<MessageRecord> {
    public MessageViewAdapter(Context context, List<MessageRecord> objects) {
        super(context, 0, objects);
        setNotifyOnChange(true);
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        MessageRecord record = getItem(getCount() - (pos + 1)); //reverse the order of the messages

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_view, parent, false);
        }
        TextView txtName = convertView.findViewById(R.id.headerField);
        TextView txtBody = convertView.findViewById(R.id.bodyField);

        txtName.setText(record.getMessage().getSource());
        txtBody.setText(record.getMessage().getMessageBody());
        return convertView;
    }

}
