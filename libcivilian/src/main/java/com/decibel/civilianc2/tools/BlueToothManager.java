package com.decibel.civilianc2.tools;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlueToothManager {
    public BlueToothManager(Activity activity){
        this.activity = activity;
    }

    public boolean start() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        return true;
    }

    public List<BluetoothDevice> getConnectedDevices(){
        ArrayList<BluetoothDevice> results = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                results.add(device);
            }
        }
        return results;
    }



    private Activity activity;
    public final static int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter bluetoothAdapter;
}
