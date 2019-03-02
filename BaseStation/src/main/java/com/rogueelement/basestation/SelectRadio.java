package com.rogueelement.basestation;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.decibel.civilianc2.radios.BluetoothSerialComms;
import com.decibel.civilianc2.radios.RSUV3;
import com.decibel.civilianc2.tools.BlueToothManager;
import com.rogueelement.basestation.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

public class SelectRadio extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_radio);
        if(blueToothManager == null) {
            blueToothManager = new BlueToothManager(this);
            if (blueToothManager.start()) {
                updateList();
            } else {
                Toast.makeText(this.getBaseContext(), "Bluetooth not supported on this device.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateList(){
        final List<BluetoothDevice> devices = blueToothManager.getConnectedDevices();
        final Context context = this.getApplicationContext();
        Thread connectThread = new Thread(){
            public void run(){
                for(BluetoothDevice device : devices){
                    try {
                        BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
                        socket.connect();
                        BluetoothSerialComms serialComms = new BluetoothSerialComms(socket);
                    } catch (IOException e){
                        e.printStackTrace();

                    }
                }
            }
        };
        connectThread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == BlueToothManager.REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                updateList();
            } else {
                Toast.makeText(this.getBaseContext(), "Bluetooth detection deactivated.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static BlueToothManager blueToothManager;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
}
