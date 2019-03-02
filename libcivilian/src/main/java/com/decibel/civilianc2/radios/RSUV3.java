package com.decibel.civilianc2.radios;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by dburnett on 4/18/2018.
 */

public class RSUV3 implements ITransceiver {

    public RSUV3(Context context){
        this.context = context;
    }

    @Override
    public int getTransmitFreq() {
        return txFrequency;
    }

    @Override
    public void setTransmitFreq(int transmitFreq) throws IOException {
        serialComms.send(SetTransmitFrequencyCommand + toFrequencyText(transmitFreq));
        txFrequency = transmitFreq;
        for(IEventListener listener : listeners){
            listener.onFrequencyChanged(rxFrequency, txFrequency, squelchLevel);
        }
    }


    @Override
    public void setFrequency(int rx, int tx) throws IOException {
        serialComms.send(SetTransmitFrequencyCommand + toFrequencyText(tx));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        serialComms.send(SetReceiveFrequencyCommand + toFrequencyText(rx));
        rxFrequency = rx;
        txFrequency = tx;
        for(IEventListener listener : listeners){
            listener.onFrequencyChanged(rxFrequency, txFrequency, squelchLevel);
        }
    }

    @Override
    public void setReceiveFreq(int receiveFreq) throws IOException {
        serialComms.send(SetReceiveFrequencyCommand + toFrequencyText(receiveFreq));
        rxFrequency = receiveFreq;
        for(IEventListener listener : listeners){
            listener.onFrequencyChanged(rxFrequency, txFrequency, squelchLevel);
        }
    }


    @Override
    public void setToneSquelch(int frequency) throws IOException{
        serialComms.send(SetCTCSSToneCommand + frequency);
        this.ctcssFrequency = frequency;
    }

    @Override
    public void enableToneSquelch(boolean enable) throws IOException{
        serialComms.send(SetCTCSSMode + (enable ? "1" : "0"));
        this.ctcssEnabled = enable;
    }

    @Override
    public int getToneSquelchFrequency() {
        return ctcssFrequency;
    }

    @Override
    public boolean getToneSquelchEnabled() {
        return ctcssEnabled;
    }



    @Override
    public ISoftwareTransmitter getSoftwareTransmitter(){
        return this.softwareTransmitter;
    }

    private class SoftwareTransmitter implements ISoftwareTransmitter{
        @Override
        public void setMode(Mode mode) throws IOException {
            switch (mode){
                case TRANSMIT:
                    serialComms.send(SetTranmitterStateCommand + "1");
                    break;
                case RECEIVE:
                    serialComms.send(SetTranmitterStateCommand + "0");
            }
        }
    }


    @Override
    public String getName() {
        return "RS-UV3A";
    }



    @Override
    public int getReceiveFreq() {
        return rxFrequency;
    }

    @Override
    public int getVolume() {
        return volumeLevel;
    }

    @Override
    public void setVolume(int v) throws IOException {
        if(v >=  VolMin && v <= VolMax) {
            serialComms.send(SetVolumeLevelCommand + String.format("%02d", v));
            volumeLevel = v;
        }
    }

    @Override
    public void setSquelch(int s) throws IOException {
        if(s >= SquelchMin && s <= SqelchMax) {
            serialComms.send(SetSquelchLevelCommand + s);
            squelchLevel = s;
        }
    }

    @Override
    public int getSquelch() {
        return squelchLevel;
    }

    @Override
    public int getSignalLevel() {
        return this.signalStrength;
    }

    @Override
    public boolean scan(int freq) {
        return false;
    }

    @Override
    public boolean isConnected() throws IOException{
        String signal;
        signal = serialComms.sendCommand(QuerySignalLevelCommand);
        if(signal == null) throw new IOException("Radio returned null for signal level.");
        if(!signal.contains(SignalStrengthTranmitting))
            this.signalStrength = dbToSignalLevel(parseIntResponse(signal)); //just any query would do, let's check temp
        return true;
    }


    public static UsbDevice getUsbDevice(UsbManager manager){
        Map<String, UsbDevice> connectedDevices = manager.getDeviceList();
        for (UsbDevice device : connectedDevices.values()) {
            if (device.getVendorId() == VendorID && device.getProductId() == ProductID) {
                return device;
            }
        }
        return null;
    }


    void startSerialConnection(ISerialComms serialComms) throws IOException {
        this.serialComms = serialComms;

        this.currentFirmware = serialComms.sendCommand(FirmwareCommand);
        String response = serialComms.sendCommand(QueryFrequenciesCommand);
        Scanner scanner = new Scanner(response);
        scanner.next("RX:");
        this.rxFrequency = scanner.nextInt();
        scanner.next("TX:");
        this.txFrequency = scanner.nextInt();
        serialComms.send(SetSquelchRangeHigh);
        this.ctcssFrequency = parseIntResponse(serialComms.sendCommand(QueryCTCSSFrequencyCommand));
        this.volumeLevel = parseIntResponse(serialComms.sendCommand(QueryVolumeCommand));
        this.squelchLevel = parseIntResponse(serialComms.sendCommand(QuerySquelchCommand));
        this.ctcssEnabled = parseIntResponse(serialComms.sendCommand(QueryCTCSSMode)) != 0;
        //serialComms.send(ArduinoSerialPortBaudRateCommand + "2");
        //serialComms.sendCommand("ST0");
    }

    @Override
    public void addEventListener(IEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(IEventListener listener) {
        listeners.remove(listener);
    }

    private String toFrequencyText(int freq){
        return String.format("%06d", freq);
    }

    private int parseIntResponse(String commandResponse){
        int index = commandResponse.indexOf(":");
        String response = commandResponse.substring(index + 1, commandResponse.length()).trim();
        return Integer.parseInt(response);
    }

    private int dbToSignalLevel(int db){
        int invert = db + 100;
        int scale = invert * (100/14);
        return scale > 100 ? 100 : scale;
    }

    private int toFahrenheit(int celsius){
        return (int)((celsius * 9.0/5.0) + 32.0);
    }

    private Context context;
    private ISerialComms serialComms;
    //UsbSerialDevice serial;
    private Object messageWaiter = new Object();
    private int rxFrequency;
    private int txFrequency;
    private int ctcssFrequency;
    private int volumeLevel;
    private int squelchLevel;
    private boolean ctcssEnabled = false;
    private int boardTemperature;
    private int signalStrength;
    private SoftwareTransmitter softwareTransmitter = new SoftwareTransmitter();

    private List<IEventListener> listeners = new ArrayList<>();

    private String currentFirmware;
    private String currentTemperature;
    private String currentResponseBuffer;

    private static final String AudioLowPassFilterCommand       = "AF";
    private static final String ArduinoInputPinFunctionCommand  = "AI";
    private static final String ArduinoOutputPinFunctionCommand = "AO";
    private static final String ArduinoSerialPortBaudRateCommand= "B1";
    private static final String IOConnectorBaudRateCommand      = "B2";
    private static final String SetCWBeaconTimer                = "BC";
    private static final String SendControlToBootloader         = "BL";
    private static final String SetBeaconMessage                = "BM";
    private static final String SetMCWBeaconTimer               = "BT";
    private static final String SetChannelBandwidth             = "BW";
    private static final String SetCourtesyBeepMode             = "CB";


    private static final String SetTransmitFrequencyCommand     = "FT";
    private static final String SetReceiveFrequencyCommand      = "FR";
    private static final String SetVolumeLevelCommand           = "VU";
    private static final String SetTranmitterStateCommand       = "TX";
    private static final String SetSquelchLevelCommand          = "SQ";
    private static final String QueryFrequenciesCommand         = "F?";
    private static final String QueryFrequenciesResponse        = "RX";
    private static final String SetCTCSSToneCommand             = "TF";
    private static final String QueryCTCSSFrequencyCommand      = "TF?";
    private static final String SetCTCSSMode                    = "TM";
    private static final String QueryCTCSSMode                  = "TM?";
    private static final String QueryVolumeCommand              = "VU?";
    private static final String QuerySquelchCommand             = "SQ?";
    private static final String QuerySignalLevelCommand         = "SS";

    private static final String SetSquelchRangeHigh             = "SR1";
    private static final String SetSquelchRangeLow              = "SR0";

    private static final String SignalStrengthTranmitting       = "TX ON";

    private static final String FirmwareCommand = "FW";
    private static final String BoardTemperatureCommand = "TP";
    public static final int ProductID = 24597;
    public static final int VendorID = 1027;

    private static final int SquelchMin = 0;
    private static final int SqelchMax = 9;

    private static final int VolMin = 0;
    private static final int VolMax = 39;

}
