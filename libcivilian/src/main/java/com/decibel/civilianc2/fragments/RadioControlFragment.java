package com.decibel.civilianc2.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.decibel.civilianc2.controls.EditFrequencyView;
import com.decibel.civilianc2.controls.NumericTouchControl;
import com.decibel.civilianc2.model.dataaccess.Channels;
import com.decibel.civilianc2.model.managers.ChannelManager;
import com.decibel.civilianc2.radios.Channel;
import com.decibel.civilianc2.radios.IReceiver;
import com.decibel.civilianc2.radios.ISerialCommsListener;
import com.decibel.civilianc2.radios.ISoftwareTransmitter;
import com.decibel.civilianc2.radios.ITransceiver;
import com.decibel.civilianc2.radios.RadioController;
import com.decibel.civilianc2.radios.RadioDetection;
import com.decibel.civilianc2.tools.BlueToothManager;
import com.example.libcivilian.R;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Activity.RESULT_OK;

public class RadioControlFragment extends Fragment implements EditFrequencyView.OnTuneListener, IReceiver.IEventListener, CompoundButton.OnCheckedChangeListener, ISerialCommsListener{


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.activity_radio_control, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        context = this.getActivity().getApplicationContext();
        this.editFrequencyView = view.findViewById(R.id.editFrequency);
        this.btnOffsetPlus = view.findViewById(R.id.btnPlusOffset);
        this.btnOffsetOff = view.findViewById(R.id.btnOffsetZero);
        this.btnOffsetMinus = view.findViewById(R.id.btnMinusOffset);
        this.txtComms = view.findViewById(R.id.editComms);
        this.commsScroller = view.findViewById(R.id.commsScroller);
        this.lblBand = view.findViewById(R.id.lblBand);
        this.volumeControl = view.findViewById(R.id.volumeControl);
        this.squelchControl = view.findViewById(R.id.squelchControl);
        this.ctcssControl = view.findViewById(R.id.ctcssControl);
        this.ctcssEnable = view.findViewById(R.id.enableCTCSS);
        this.lblRadioStatus = view.findViewById(R.id.lblRadioStatus);
        this.btnPtt = view.findViewById(R.id.btnPTT);
        this.btnSaveChannel = view.findViewById(R.id.btnSaveChannel);
        this.signalLevel = view.findViewById(R.id.imgSignal);
        this.txtComms.setMovementMethod(new ScrollingMovementMethod());
        this.btnOffsetPlus.setOnCheckedChangeListener(this);
        this.btnOffsetOff.setOnCheckedChangeListener(this);
        this.btnOffsetMinus.setOnCheckedChangeListener(this);
        this.editFrequencyView.setOnTuneListener(this);
        this.editFrequencyView.setFrequencyDisplay(140000);
        this.volumeControl.setIncrement(1);
        this.volumeControl.setOnRequestChangeListener(new NumericTouchControl.OnRequestChangeListener() {
            @Override
            public void onRequestChange(int increment) {
                try {
                    int targetVol = tranceiver.getTransceiver().getVolume() + increment;
                    tranceiver.getTransceiver().setVolume(targetVol);
                    volumeControl.setValue(tranceiver.getTransceiver().getVolume());
                }catch (Exception e){
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        this.squelchControl.setIncrement(1);
        this.squelchControl.setOnRequestChangeListener(new NumericTouchControl.OnRequestChangeListener() {
            @Override
            public void onRequestChange(int increment) {
                try{
                    int targetSquelch = tranceiver.getTransceiver().getSquelch() + increment;
                    tranceiver.getTransceiver().setSquelch(targetSquelch);
                    squelchControl.setValue(tranceiver.getTransceiver().getSquelch());
                }catch (Exception e){
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        this.ctcssControl.setIncrement(1);
        this.ctcssControl.setValue(' ');
        this.ctcssControl.setOnRequestChangeListener(new NumericTouchControl.OnRequestChangeListener() {
            @Override
            public void onRequestChange(int increment) {
                try {
                    tranceiver.incrementCtcss(increment > 0);
                    ctcssControl.setValue(tranceiver.getTransceiver().getToneSquelchFrequency()/100.0);
                } catch (Exception e){
                    onDisconnect();
                }
            }
        });

        /*final RadioControlFragment that = this;
        RadioDetection.getConnectedDevices(this.getActivity(), this, new RadioDetection.IOnRadioDetectedHandler() {
            @Override
            public void onRadioDetected(List<ITransceiver> connectedDevices) {
                if(connectedDevices.size() > 0) {
                    try {
                        ITransceiver radio = connectedDevices.get(0);
                        radioConnectedLastCheck = true;
                        //setTitle(radio.getName());
                        tranceiver = new RadioController(radio);
                        tranceiver.getTransceiver().addEventListener(that);

                        if (radioConnectedLastCheck) {
                            lblRadioStatus.setText("CONNECTED");
                        }

                        editFrequencyView.setFrequencyDisplay(tranceiver.getTransceiver().getReceiveFreq());
                        lblBand.setText(tranceiver.getBandString());
                        volumeControl.setValue(tranceiver.getTransceiver().getVolume());
                        squelchControl.setValue(tranceiver.getTransceiver().getSquelch());
                        if (tranceiver.getTransceiver().getToneSquelchEnabled())
                            ctcssControl.setValue(tranceiver.getTransceiver().getToneSquelchFrequency() / 100.0);
                        if (tranceiver.getOffset() == RadioController.Offset.Plus) {
                            btnOffsetPlus.setChecked(true);
                        } else if (tranceiver.getOffset() == RadioController.Offset.Minus) {
                            btnOffsetMinus.setChecked(true);
                        }
                        ctcssEnable.setChecked(tranceiver.getTransceiver().getToneSquelchEnabled());
                    }
                    catch (Exception e){
                        onDisconnect();
                    }
                    checkConnectionStatus();
                }
            }
        });*/

        this.ctcssEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    tranceiver.getTransceiver().enableToneSquelch(isChecked);
                    if(!isChecked){
                        ctcssControl.setValue(' ');
                    } else {
                        ctcssControl.setValue(tranceiver.getTransceiver().getToneSquelchFrequency()/100.0);
                    }
                } catch (Exception e) {
                    onDisconnect();
                }
            }
        });

        this.btnPtt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        tranceiver.getTransceiver().getSoftwareTransmitter().setMode(ISoftwareTransmitter.Mode.TRANSMIT);
                    } catch (Exception e) {
                        onDisconnect();
                    }
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    try {
                        tranceiver.getTransceiver().getSoftwareTransmitter().setMode(ISoftwareTransmitter.Mode.RECEIVE);
                    } catch (Exception e) {
                        onDisconnect();
                    }
                }
                return false;
            }
        });

        this.btnSaveChannel.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                doSaveChannel();
            }
        });
    }

    public void setRadio(ITransceiver transceiver){

        try {
            radioConnectedLastCheck = true;
            //setTitle(radio.getName());
            tranceiver = new RadioController(transceiver);
            tranceiver.getTransceiver().addEventListener(this);

            if (radioConnectedLastCheck) {
                lblRadioStatus.setText("CONNECTED");
            }

            editFrequencyView.setFrequencyDisplay(tranceiver.getTransceiver().getReceiveFreq());
            lblBand.setText(tranceiver.getBandString());
            volumeControl.setValue(tranceiver.getTransceiver().getVolume());
            squelchControl.setValue(tranceiver.getTransceiver().getSquelch());
            if (tranceiver.getTransceiver().getToneSquelchEnabled())
                ctcssControl.setValue(tranceiver.getTransceiver().getToneSquelchFrequency() / 100.0);
            else
                ctcssControl.setEnabled(false);
            if (tranceiver.getOffset() == RadioController.Offset.Plus) {
                btnOffsetPlus.setChecked(true);
            } else if (tranceiver.getOffset() == RadioController.Offset.Minus) {
                btnOffsetMinus.setChecked(true);
            } else {
                btnOffsetOff.setChecked(true);
            }
            ctcssEnable.setChecked(tranceiver.getTransceiver().getToneSquelchEnabled());
            checkConnectionStatus();
        } catch (Exception e) {
            onDisconnect();
        }
    }

    private void checkConnectionStatus() {
        Timer timer = new Timer();
        final ISerialCommsListener that = this;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            tranceiver.getTransceiver().isConnected();
                            radioConnectedLastCheck = true;
                        } catch (IOException e) {
                            radioConnectedLastCheck = false;
                        }
                        lblRadioStatus.setText(radioConnectedLastCheck ? "CONNECTED" : "DISCONNECTED");
                        int signal = ((int)(tranceiver.getTransceiver().getSignalLevel()) / 10) * 10;
                        int drawableResourceId = getResources().getIdentifier("signal_" + signal, "drawable", context.getPackageName());
                        signalLevel.setImageResource(drawableResourceId);

                    }
                });
            }
        }, 5000, 2000);
    }

    private void onDisconnect(){
        lblRadioStatus.setText("DISCONNECTED");
        Toast.makeText(context, "Lost Connection with device.", Toast.LENGTH_LONG).show();
        radioConnectedLastCheck = false;
    }

    private void doSaveChannel(){
        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this.getActivity());
        builder.setTitle("Channel");


        View viewInflated = LayoutInflater.from(context).inflate(R.layout.channel_name_input, null, false);
// Set up the input
        final EditText input = (EditText) viewInflated.findViewById(R.id.editChannelName);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(viewInflated);

// Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String m_Text = input.getText().toString();
                ITransceiver radio = tranceiver.getTransceiver();
                Integer toneSquelch = (radio.getToneSquelchEnabled() ? radio.getToneSquelchFrequency() : null);
                if(channelManager.addChannel(new Channel(m_Text, null, radio.getTransmitFreq(), radio.getReceiveFreq(), toneSquelch, toneSquelch))){
                    Toast.makeText(context, "Added " + m_Text + " to your channels list.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Failed to add " + m_Text + " to your channels. Try a different name.", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }


    @Override
    public void onTuneRequested(int increment) {
        if(tranceiver != null) {
            int targetFreq = tranceiver.getTransceiver().getReceiveFreq() + increment;
            try {
                tranceiver.setFrequency(targetFreq);
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onComms(final String message, final boolean transmitted) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(transmitted)
                    txtComms.append("[TX]" + message + "\n");
                else
                    txtComms.append("[RX]" + message + "\n");
                commsScroller.smoothScrollTo(0, txtComms.getBottom() + 500);
            }
        });
    }

    @Override
    public void onFrequencyChanged(final int rx, int tx, int sql) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                editFrequencyView.setFrequencyDisplay(rx);
                lblBand.setText(tranceiver.getBandString());
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            if (buttonView == this.btnOffsetPlus && isChecked == true) {
                tranceiver.setOffset(RadioController.Offset.Plus);
            } else if (buttonView == btnOffsetOff && isChecked == true) {
                tranceiver.setOffset(RadioController.Offset.Off);
            } else if (buttonView == btnOffsetMinus && isChecked == true) {
                tranceiver.setOffset(RadioController.Offset.Minus);
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == BlueToothManager.REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

            } else {
                Toast.makeText(context, "Bluetooth detection deactivated.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void setChannelManager(ChannelManager channelManager){
        this.channelManager = channelManager;
    }

    private Context context;
    private RadioController tranceiver;
    private EditFrequencyView editFrequencyView;
    private RadioButton btnOffsetPlus;
    private RadioButton btnOffsetOff;
    private RadioButton btnOffsetMinus;
    private Button btnPtt;
    private Button btnSaveChannel;
    private TextView txtComms;
    private ScrollView commsScroller;
    private TextView lblBand;
    private ImageView signalLevel;
    private NumericTouchControl volumeControl;
    private NumericTouchControl squelchControl;
    private NumericTouchControl ctcssControl;
    private CheckBox ctcssEnable;
    private TextView lblRadioStatus;
    private Handler handler = new Handler();
    private boolean radioConnectedLastCheck = false;
    private ChannelManager channelManager;
}
