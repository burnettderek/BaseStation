package com.decibel.civilianc2.activities;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.decibel.civilianc2.R;
import com.decibel.civilianc2.model.dataaccess.LocationReports;
import com.decibel.civilianc2.model.dataaccess.Stations;
import com.decibel.civilianc2.model.managers.MessageInterpreter;
import com.decibel.civilianc2.model.managers.Model;
import com.decibel.civilianc2.model.managers.PositionManager;
import com.decibel.civilianc2.model.managers.StationManager;
import com.decibel.civilianc2.modems.fsk.Demodulator;
import com.decibel.civilianc2.modems.fsk.IDemodulatorListener;
import com.decibel.civilianc2.protocols.ax25.AX25Packet;
import com.decibel.civilianc2.protocols.ax25.AX25Decoder;
import com.decibel.civilianc2.tools.Database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import sivantoledo.ax25.Packet;

public class FSKModemTest extends Activity implements IDemodulatorListener {

    protected Demodulator modem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fskmodem_test);
        txtComms = (TextView)findViewById(R.id.txtComms);
        txtComms.setMovementMethod(new ScrollingMovementMethod());
        txtAudioLevel = (TextView)findViewById(R.id.txtAudioLevel);
        btnTestMessage = (Button)findViewById(R.id.btnTestMessage);
        btnTestMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    InputStream stream = getAssets().open("ObjectDefinition.aprs");
                    byte[] bytes = new byte[1024];
                    int read = stream.read(bytes, 0, 1024);
                    bytes = Arrays.copyOf(bytes, read);
                    Model.getInstance().getMessageInterpreter().onPacketReceived(bytes);
                    onPacketReceived(bytes);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Button startDemod = (Button)findViewById(R.id.btnDemodStart);
        Demodulator.getInstance().addListener(this);
        startDemod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Demodulator.getInstance().startListeningToMic();
            }
        });
        Button stopDemod = (Button)findViewById(R.id.btnDemodStop);
        stopDemod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Demodulator.getInstance().stopListeningToMic();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Demodulator.getInstance().removeListener(this);
    }

    @Override
    public void onAudioLevelChanged(final int audioLevel) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                txtAudioLevel.setText(Integer.toString(audioLevel));
            }
        });

    }

    @Override
    public void onPacketReceived(final byte[] bytes) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                /*try {
                    File file = new File(Environment.getExternalStorageDirectory(), "ObjectDefinition.aprs");
                    FileOutputStream stream = new FileOutputStream(file);
                    stream.write(bytes, 0, bytes.length);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }*/
                /*StringBuilder hexOut = new StringBuilder();
                for(Byte byteVal : bytes){
                    hexOut.append(String.format("0x%02x ", byteVal));
                }*/
                //txtComms.setText(txtComms.getText() + hexOut.toString() + "\n");
                txtComms.setText(txtComms.getText() + Packet.format(bytes) + "\n");
                try {
                    AX25Decoder decoder = new AX25Decoder();
                    AX25Packet packet = decoder.Decode(bytes);
                    txtComms.setText(txtComms.getText() + "\n" + packet);
                }
                catch (Exception e){
                    txtComms.setText(txtComms.getText() + e.getMessage() + "\n");
                }
            }
        });
    }

    @Override
    public void onRunningChanged(boolean running) {

    }

    private TextView txtComms;
    private TextView txtAudioLevel;
    private Button btnTestMessage;
    private Handler handler = new Handler();
}
