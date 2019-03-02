package com.decibel.civilianc2.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.decibel.civilianc2.model.managers.Model;
import com.decibel.civilianc2.radios.Channel;
import com.example.libcivilian.R;

public class AddChannel extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_channel);



        final EditText txtChannelName = findViewById(R.id.txtChannelName);
        final EditText txtChanndelDesc = findViewById(R.id.txtChannelDesc);
        final EditText txtRxFreq = findViewById(R.id.txtChannelRx);
        final EditText txtTxFreq = findViewById(R.id.txtChannelTx);
        final Button btnAddChannel = findViewById(R.id.btnAddChannel);

        txtRxFreq.setText(this.getIntent().getStringExtra("Channel.Frequency.RX"));
        txtTxFreq.setText(this.getIntent().getStringExtra("Channel.Frequency.TX"));
        btnAddChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String name = txtChannelName.getText().toString();
                    String desc = txtChanndelDesc.getText().toString();
                    String rxFreq = txtRxFreq.getText().toString();
                    String txFreq = txtTxFreq.getText().toString();
                    int rx = (int)(Double.parseDouble(rxFreq) * 1000);
                    int tx = (int)(Double.parseDouble(txFreq) * 1000);
                    if(currentChannel == null) {
                        Model.getInstance().getChannels().add(new Channel(name, desc, tx, rx, null, null));
                        Toast.makeText(getBaseContext(), "Added channel", Toast.LENGTH_LONG).show();
                    }
                    else //we're updating an existing channel
                    {
                        Model.getInstance().getChannels().update(new Channel(name, desc, tx, rx, null, null));
                        Toast.makeText(getBaseContext(), "Updated channel '" + name + "'.", Toast.LENGTH_LONG).show();
                    }
                    finish();
                }
                catch (Exception e){
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });

        Button offsetPlus = findViewById(R.id.btnOffsetPlus);
        offsetPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    double rxFreq = Double.parseDouble(txtRxFreq.getText().toString());
                    txtTxFreq.setText(String.format("%.3f", rxFreq + freqOffset));
                }catch (Exception e){
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button offsetMinus = findViewById(R.id.btnOffsetMinus);
        offsetMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    double rxFreq = Double.parseDouble(txtRxFreq.getText().toString());
                    txtTxFreq.setText(String.format("%.3f", rxFreq - freqOffset));
                }catch (Exception e){
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        String channelName = getIntent().getStringExtra("Channel.Name");
        if(channelName != null) {
            currentChannel = Model.getInstance().getChannels().getChannel(channelName);
            txtChannelName.setText(currentChannel.getName());
            txtChannelName.setEnabled(false);
            txtChanndelDesc.setText(currentChannel.getDescription());
            txtRxFreq.setText(String.format("%.3f", currentChannel.getRxFreq()));
            txtTxFreq.setText(String.format("%.3f", currentChannel.getTxFreq()));
            btnAddChannel.setText("Update Channel");
        }
    }

    private Channel currentChannel = null;
    private double freqOffset = 0.600;
}
