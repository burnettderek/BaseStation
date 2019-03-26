package com.decibel.civilianc2.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.decibel.civilianc2.radios.IReceiver;
import com.decibel.civilianc2.radios.ISerialCommsListener;
import com.decibel.civilianc2.radios.ISoftwareTransmitter;
import com.decibel.civilianc2.radios.ITransceiver;
import com.decibel.civilianc2.radios.MockRadio;
import com.decibel.civilianc2.radios.RSUV3;
import com.decibel.civilianc2.radios.RadioController;
import com.decibel.civilianc2.radios.RadioDetection;
import com.decibel.civilianc2.tools.BlueToothManager;
import com.example.libcivilian.R;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RadioControl extends AppCompatActivity implements EditFrequencyView.OnTuneListener, IReceiver.IEventListener, CompoundButton.OnCheckedChangeListener, ISerialCommsListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_control);
        setTitle("Connecting...");
        this.editFrequencyView = this.findViewById(R.id.editFrequency);
        this.btnOffsetPlus = this.findViewById(R.id.btnPlusOffset);
        this.btnOffsetOff = this.findViewById(R.id.btnOffsetZero);
        this.btnOffsetMinus = this.findViewById(R.id.btnMinusOffset);
        this.txtComms = this.findViewById(R.id.editComms);
        this.commsScroller = this.findViewById(R.id.commsScroller);
        this.lblBand = this.findViewById(R.id.lblBand);
        this.volumeControl = this.findViewById(R.id.volumeControl);
        this.squelchControl = this.findViewById(R.id.squelchControl);
        this.ctcssControl = this.findViewById(R.id.ctcssControl);
        this.ctcssEnable = this.findViewById(R.id.enableCTCSS);
        this.lblRadioStatus = this.findViewById(R.id.lblRadioStatus);
        this.btnPtt = this.findViewById(R.id.btnPTT);
        this.btnSaveChannel = this.findViewById(R.id.btnSaveChannel);
        this.signalLevel = this.findViewById(R.id.imgSignal);
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
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
                } catch (IOException e){
                    onDisconnect();
                }
            }
        });

        final RadioControl that = this;
        RadioDetection.getConnectedDevices(this, this, new RadioDetection.IOnRadioDetectedHandler() {
            @Override
            public void onRadioDetected(List<ITransceiver> connectedDevices) {
                if(connectedDevices.size() > 0) {
                    try {
                        ITransceiver radio = connectedDevices.get(0);
                        radioConnectedLastCheck = true;
                        setTitle(radio.getName());
                        tranceiver = new RadioController(radio);
                        tranceiver.getTransceiver().addEventListener(that);

                        if (radioConnectedLastCheck) {
                            lblRadioStatus.setText("CONNECTED");
                        }

                        editFrequencyView.setFrequencyDisplay(tranceiver.getTransceiver().getReceiveFreq());
                        lblBand.setText(tranceiver.getBandString());
                        volumeControl.setValue(tranceiver.getTransceiver().getVolume());
                        squelchControl.setValue(tranceiver.getTransceiver().getSquelch());
                        if (tranceiver.getTransceiver().getToneSquelchFrequency() != null)
                            that.ctcssControl.setValue(tranceiver.getTransceiver().getToneSquelchFrequency() / 100.0);
                        if (tranceiver.getOffset() == RadioController.Offset.Plus) {
                            that.btnOffsetPlus.setChecked(true);
                        } else if (tranceiver.getOffset() == RadioController.Offset.Minus) {
                            that.btnOffsetMinus.setChecked(true);
                        }
                        that.ctcssEnable.setChecked(that.tranceiver.getTransceiver().getToneSquelchFrequency() != null);
                    }
                    catch (Exception e){
                        onDisconnect();
                    }
                    checkConnectionStatus();
                }
            }
        });

        this.ctcssEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    tranceiver.incrementCtcss(true);
                } catch (IOException e) {
                    onDisconnect();
                }
                if(!isChecked){
                    ctcssControl.setValue(' ');
                } else {
                    ctcssControl.setValue(tranceiver.getTransceiver().getToneSquelchFrequency()/100.0);
                }
            }
        });

        this.btnPtt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        tranceiver.getTransceiver().getSoftwareTransmitter().setMode(ISoftwareTransmitter.Mode.TRANSMIT);
                    } catch (IOException e) {
                        onDisconnect();
                    }
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    try {
                        tranceiver.getTransceiver().getSoftwareTransmitter().setMode(ISoftwareTransmitter.Mode.RECEIVE);
                    } catch (IOException e) {
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

    private void checkConnectionStatus() {
        Timer timer = new Timer();
        final ISerialCommsListener that = this;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    tranceiver.getTransceiver().isConnected();
                    radioConnectedLastCheck = true;
                } catch (IOException e) {
                    radioConnectedLastCheck = false;
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        lblRadioStatus.setText(radioConnectedLastCheck ? "CONNECTED" : "DISCONNECTED");
                        int signal = ((int)(tranceiver.getTransceiver().getSignalLevel()) / 10) * 10;
                        int drawableResourceId = getResources().getIdentifier("signal_" + signal, "drawable", getPackageName());
                        signalLevel.setImageResource(drawableResourceId);

                    }
                });
            }
        }, 5000, 2000);
    }

    private void onDisconnect(){
        lblRadioStatus.setText("DISCONNECTED");
        Toast.makeText(getApplicationContext(), "Lost Connection with device.", Toast.LENGTH_LONG).show();
        radioConnectedLastCheck = false;
    }

    private void doSaveChannel(){
        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("Channel");

        View viewInflated = LayoutInflater.from(getBaseContext()).inflate(R.layout.channel_name_input, null, false);
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
        int targetFreq = tranceiver.getTransceiver().getReceiveFreq() + increment;
        try {
            tranceiver.setFrequency(targetFreq);
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == BlueToothManager.REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

            } else {
                Toast.makeText(this.getBaseContext(), "Bluetooth detection deactivated.", Toast.LENGTH_LONG).show();
            }
        }
    }

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


}
