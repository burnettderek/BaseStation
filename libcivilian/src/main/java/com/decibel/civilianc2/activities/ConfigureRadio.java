package com.decibel.civilianc2.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.decibel.civilianc2.adaptors.ChannelViewAdapter;
import com.decibel.civilianc2.controls.ScannerView;
import com.decibel.civilianc2.model.managers.Model;
import com.decibel.civilianc2.radios.Channel;
import com.decibel.civilianc2.radios.IReceiver;
import com.decibel.civilianc2.radios.ISoftwareTransmitter;
import com.decibel.civilianc2.radios.ITransceiver;
import com.decibel.civilianc2.radios.ITransmitter;
import com.decibel.civilianc2.radios.RSUV3;
import com.example.libcivilian.R;

import java.io.IOException;
import java.util.List;

public class ConfigureRadio extends Activity implements AdapterView.OnItemSelectedListener, IReceiver.IEventListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_radio);
        //String driver = getIntent().getStringExtra("radio.driver");

        radio = new RSUV3();

        btnSetTxFreq = findViewById(R.id.btnSetTxFeq);
        btnSetRxFreq = findViewById(R.id.btnSetRxFreq);

        txtTxFreq = findViewById(R.id.txtTxFreq);
        txtRxFreq = findViewById(R.id.txtRxFreq);
        ((TextView)findViewById(R.id.RadioNameLabel)).setText(radio.getName());
        txtConnectionStatus = findViewById(R.id.txtConnectionStatus);


        try {
            boolean connectResult = true;
            ((TextView)findViewById(R.id.RadioNameLabel)).setText(radio.getName());
            if(connectResult) {
                txtConnectionStatus.setText("CONNECTED");
                txtConnectionStatus.setBackgroundColor(Color.GREEN);
                txtConnectionStatus.setTextColor(Color.BLACK);
            }
            else {
                txtConnectionStatus.setText("ERROR CONNECTING");
                txtConnectionStatus.setBackgroundColor(Color.YELLOW);
                txtConnectionStatus.setTextColor(Color.BLACK);
            }

        }
        catch (Exception e) {
            txtConnectionStatus.setText(e.getMessage());
        }

        txtTxFreq.setText(String.format("%06.3f", radio.getTransmitFreq()));
        txtRxFreq.setText(String.format("%06.3f", radio.getReceiveFreq()));

        radioComms = findViewById(R.id.txtRadioComms);
        radioComms.setMovementMethod(new ScrollingMovementMethod());

        radioScroller = findViewById(R.id.radioCommScroller);



        btnSetTxFreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String val = txtTxFreq.getText().toString();
                int txFreq = (int)(Double.parseDouble(val) * 1000);
                try {
                    radio.setTransmitFreq(txFreq);
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        btnSetRxFreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String val = txtRxFreq.getText().toString();
                int rxFreq = (int)(Double.parseDouble(val) * 1000);
                try {
                    radio.setReceiveFreq(rxFreq);
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });


        final Button ptt = findViewById(R.id.btnPTT);
        ptt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    try {
                        radio.getSoftwareTransmitter().setMode(ISoftwareTransmitter.Mode.TRANSMIT);
                    } catch (IOException e){}
                    ptt.setPressed(true);
                    return true;
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    try {
                        radio.getSoftwareTransmitter().setMode(ISoftwareTransmitter.Mode.RECEIVE);
                    } catch (IOException e){}
                    ptt.setPressed(false);
                    return true;
                }
                return false;
            }
        });

        SeekBar volumeControl = findViewById(R.id.VolumeControl);
        volumeControl.setProgress(radio.getVolume());
        //volumeControl.setProgress(50);
        volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    radio.setVolume(seekBar.getProgress());
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        SeekBar squelchControl = findViewById(R.id.SquelchControl);
        squelchControl.setProgress(radio.getSquelch());
        squelchControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                    try {
                        radio.setSquelch(seekBar.getProgress());
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

            }
        });

        ImageView back = findViewById(R.id.btnBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button addChannel = findViewById(R.id.btnAddChannel);
        addChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddChannel();
            }
        });

        Button editChannel = findViewById(R.id.btnEditChannel);
        editChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEditChannel();
            }
        });

        Button deleteChannel = findViewById(R.id.btnDeleteChannel);
        deleteChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDeleteChannel();
            }
        });

        spinner = findViewById(R.id.listChannels);
        UpdateChannelSpinner();

        Button offsetPlus = findViewById(R.id.SetOffsetPlusButton);
        offsetPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String rxText = txtRxFreq.getText().toString();
                    int rxFreq = (int)(Double.parseDouble(rxText) * 1000);
                    int txFreq = rxFreq + (int)(freqOffset * 1000);
                    radio.setReceiveFreq(rxFreq);
                    radio.setTransmitFreq(txFreq);
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button offsetMinus = findViewById(R.id.SetOffsetMinusButton);
        offsetMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String rxText = txtRxFreq.getText().toString();
                    int rxFreq = (int)(Double.parseDouble(rxText) * 1000);
                    int txFreq = rxFreq - (int)(freqOffset * 100);
                    radio.setReceiveFreq(rxFreq);
                    radio.setTransmitFreq(txFreq);
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        ScannerView scannerView = findViewById(R.id.scannerView);
        scannerView.setRadio(radio);
        radio.addEventListener(this);
    }

    private void UpdateChannelSpinner() {
        List<Channel> channels = Model.getInstance().getChannels().getAll();
        ChannelViewAdapter adapter = new ChannelViewAdapter(this, channels);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }



    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(i != 0) {
            Channel channel = (Channel) spinner.getItemAtPosition(i - 1);
            try {
                //radio.setChannel(channel);
                Toast.makeText(getApplicationContext(), "Channel applied", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void onAddChannel(){
        Intent intent = new Intent(this, AddChannel.class);
        intent.putExtra("Channel.Frequency.RX", String.format("%06.4f", radio.getReceiveFreq()));
        intent.putExtra("Channel.Frequency.TX", String.format("%06.4f", radio.getTransmitFreq()));

        startActivity(intent);
    }

    private void onDeleteChannel() {
        int selection = spinner.getSelectedItemPosition();
        Object iObject = spinner.getItemAtPosition(selection - 1);
        if(iObject != null){
            Channel channel = (Channel)iObject;
            Model.getInstance().getChannels().remove(channel);
            UpdateChannelSpinner();
        }
    }

    private void onEditChannel() {
        int selection = spinner.getSelectedItemPosition();
        Object iObject = spinner.getItemAtPosition(selection - 1);
        if(iObject != null){
            Channel channel = (Channel)iObject;
            Intent intent = new Intent(this, AddChannel.class);
            intent.putExtra("Channel.Name", channel.getName());
            startActivity(intent);
        }
    }

    public void onRadioInterfaceComms(final String comms, final boolean command) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(command)
                    radioComms.append("[TX]" + comms + "\n");
                else
                    radioComms.append("[RX]" + comms + "\n");
                radioScroller.smoothScrollTo(0, radioComms.getBottom() + 500);
            }
        });

    }

    @Override
    public void onFrequencyChanged(final int rx, final int tx, int sql) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                txtTxFreq.setText(String.format("%.3f", tx));
                txtRxFreq.setText(String.format("%.3f", rx));
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();
        UpdateChannelSpinner();
    }

    private Button btnSetTxFreq;
    private Button btnSetRxFreq;

    EditText txtTxFreq;
    EditText txtRxFreq;

    TextView txtConnectionStatus;

    Spinner spinner;
    TextView radioComms;
    ScrollView radioScroller;
    private ITransceiver radio;
    private Handler handler = new Handler();

    private double freqOffset = 0.600;
}
