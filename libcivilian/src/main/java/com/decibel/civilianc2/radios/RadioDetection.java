package com.decibel.civilianc2.radios;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;

import com.decibel.civilianc2.tools.BlueToothManager;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class RadioDetection {
    public interface IOnRadioDetectedHandler {
        void onRadioDetected(List<ITransceiver> connectedDevices);
    }
    public static void getConnectedDevices(final Activity activity, final ISerialCommsListener serialListener, final IOnRadioDetectedHandler detectedHandler){
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final Context context = activity.getBaseContext();
                final List<ITransceiver> results = new ArrayList<>();

                getUsbDevices(context, serialListener, results);

                getBluetoothDevices(context, serialListener, results, activity);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        detectedHandler.onRadioDetected(results);
                    }
                });
            }
        });
        thread.start();
    }

    private static void getBluetoothDevices(Context context, ISerialCommsListener serialCommsListener, List<ITransceiver> results, Activity activity) {
        BlueToothManager manager = new BlueToothManager(activity);
        boolean isBluetoothAvailable = manager.start();
        if(isBluetoothAvailable == false) return;
        final List<BluetoothDevice> devices = manager.getConnectedDevices();

        for(BluetoothDevice device : devices){
            try {
                BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
                socket.connect();
                BluetoothSerialComms serialComms = new BluetoothSerialComms(socket);
                serialComms.setListener(serialCommsListener);
                RSUV3 radio = new RSUV3();
                radio.startSerialConnection(serialComms);
                results.add(radio);
            } catch (IOException e){
                e.printStackTrace();

            }
        }
    }

    private static void getUsbDevices(Context context, ISerialCommsListener commsListener, List<ITransceiver> results) {
        UsbManager usbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
        UsbDevice usbDevice = getUsbDevice(usbManager);
        if(usbDevice != null){
            UsbSerialComms usbConnection = startSerialConnection(usbManager, usbDevice);
            if(usbConnection != null) {
                RSUV3 radio = new RSUV3();
                usbConnection.setListener(commsListener);
                try {
                    radio.startSerialConnection(usbConnection);
                    results.add(radio);
                } catch (IOException e) {
                    //do nothing
                }

            }
        }
    }

    public static UsbDevice getUsbDevice(UsbManager manager){
        Map<String, UsbDevice> connectedDevices = manager.getDeviceList();
        for (UsbDevice device : connectedDevices.values()) {
            if (device.getVendorId() == RSUV3.VendorID && device.getProductId() == RSUV3.ProductID) {
                return device;
            }
        }
        return null;
    }

    public static UsbSerialComms startSerialConnection(UsbManager usbManager, UsbDevice device) {
        UsbDeviceConnection connection = usbManager.openDevice(device);
        UsbSerialDevice serial = UsbSerialDevice.createUsbSerialDevice(device, connection);

        if (serial != null && serial.open()) {
            serial.setBaudRate(19200);
            serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
            serial.setStopBits(UsbSerialInterface.STOP_BITS_1);
            serial.setParity(UsbSerialInterface.PARITY_NONE);
            serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
            UsbSerialComms serialComms = new UsbSerialComms(serial);
            return serialComms;
        }
        return null;
    }

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
}
