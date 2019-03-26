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

    public RSUV3(){
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
        if(rx == tx){
            serialComms.send(SetFrequencySimplexCommand + toFrequencyText(rx));
        } else if(tx < rx) {
            serialComms.send(SetFrequencyNegOffsetCommand + toFrequencyText(rx));
        } else {
            serialComms.send(SetFreqencyPosOffsetCommand + toFrequencyText(rx));
        }

        /*try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = serialComms.sendCommand("F?");
        if(result == null || result == "")throw new IOException("Could not verify frequency.");
        String[] rxAndTx = result.split(" TX:");

        int rxResult = parseIntResponse(rxAndTx[0]);
        int txResult = parseIntResponse(rxAndTx[1]);
        if(rxResult != rx || txResult != tx)throw new IOException("Failed to set frequency!");*/

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
    public void setToneSquelch(Integer frequency) throws IOException{
        try {
            if((frequency == null && ctcssFrequency != null) || (frequency != null && ctcssFrequency == null))//do we need to toggle CTCSS on/off?
                serialComms.send(SetCTCSSMode + (frequency != null ? "1" : "0"));
            this.ctcssFrequency = frequency;
            if(frequency == null) return;
            String freqString = frequency >= 10000 ? "" + frequency : "0" + frequency;
            serialComms.send(SetCTCSSToneCommand + freqString);
            //serialComms.send(SetCTCSSToneCommand + freqString);//note, this second blast
            //String result = serialComms.sendCommand(SetCTCSSToneCommand + "?");
            //int currentTone = parseIntResponse(result);
            //if (currentTone != frequency) throw new IOException("Failed to set the CTCSS phone.");
            this.ctcssFrequency = frequency;
        } catch (Exception e){
            throw new IOException(e);
        }
    }



    @Override
    public Integer getToneSquelchFrequency() {
        return ctcssFrequency;
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
        return "RS-UV3A: " + this.currentFirmware;
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
        try {
            serialComms.send(SetReceiveFrequencyCommand + toFrequencyText(freq));
            String response = serialComms.sendCommand(QuerySquelchState);
            int squelchLevel = parseIntResponse(response);
            return (squelchLevel == 1);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public SquelchState getSquelchState() throws IOException{
        try {
            String response = serialComms.sendCommand(QuerySquelchState);
            if (response == null) throw new IOException("Bad response from radio");
            int squelchLevel = parseIntResponse(response);
            if (squelchLevel == 1) return SquelchState.Open;
            else return SquelchState.Closed;
        } catch (Exception e) {
            throw new IOException(e);
        }
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

    @Override
    public ISerialComms getSerialComms(){
        return this.serialComms;
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

    private ISerialComms serialComms;
    private Object messageWaiter = new Object();
    private int rxFrequency;
    private int txFrequency;
    private Integer ctcssFrequency;
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


    private static final String SetFrequencySimplexCommand      = "FS";
    private static final String SetFrequencyNegOffsetCommand    = "FD";
    private static final String SetFreqencyPosOffsetCommand     = "FU";
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
    private static final String QuerySquelchState               = "SO";

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
