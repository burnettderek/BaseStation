package com.decibel.civilianc2.radios;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dburnett on 2/22/2018.
 */

public class MockRadio implements ITransceiver{
    @Override
    public int getTransmitFreq() {
        return this.frequencyTx;
    }

    @Override
    public void setTransmitFreq(int transmitFreq) throws IOException {
        this.frequencyTx = transmitFreq;
    }

    @Override
    public void setToneSquelch(int frequency) {
        this.frequencyCtcss = frequency;
        for(IEventListener listener : listeners){
            //listener.onRadioInterfaceComms(String.format("CTCSS @ %.1f", + frequency/100.0), true);
        }
    }

    @Override
    public void enableToneSquelch(boolean enable) {
        this.enableCtcss = enable;
        for(IEventListener listener : listeners){
            //listener.onRadioInterfaceComms(enable ? "Enabling CTCSS" : "Disabling CTCSS", true);
        }
    }

    @Override
    public int getToneSquelchFrequency() {
        return this.frequencyCtcss;
    }

    @Override
    public boolean getToneSquelchEnabled() {
        return enableCtcss;
    }


    @Override
    public String getName() {
        return "MOCK-7000";
    }

    @Override
    public void setReceiveFreq(int receiveFreq) throws IOException {
        this.frequencyRx = receiveFreq;
    }

    @Override
    public int getReceiveFreq() {
        return this.frequencyRx;
    }

    @Override
    public int getVolume() {
        return this.volume;
    }

    @Override
    public void setVolume(int v) {
        this.volume = v;
        //for(IEventListener listener : listeners)
            //listener.onRadioInterfaceComms("Volume changed to " + v + ".", true);
    }

    @Override
    public void setSquelch(int s) {
        this.squelch = s;
        //for(IEventListener listener : listeners){
            //.onRadioInterfaceComms("Squelch changed to " + s + ".", true);
        //}
    }

    @Override
    public int getSquelch() {
        return this.squelch;
    }

    @Override
    public SquelchState getSquelchState() throws IOException {
        return SquelchState.Closed;
    }

    @Override
    public int getSignalLevel() {
        return 50;
    }

    @Override
    public boolean scan(int freq) {
        return false;
    }

    @Override
    public boolean isConnected() throws IOException {
        return true;
    }

    @Override
    public ISoftwareTransmitter getSoftwareTransmitter(){
        return null;
    }

    @Override
    public void addEventListener(IEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(IEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void setFrequency(int rx, int tx) {
        this.frequencyRx = rx;
        this.frequencyTx = tx;
        for(IEventListener listener : listeners) {
            listener.onFrequencyChanged(rx, tx, this.squelch);
            //listener.onRadioInterfaceComms("Frequency changed to RX-" + rx + " TX-" + tx, true);
        }
    }

    private int frequencyRx = 145620;
    private int frequencyTx = 145620 - 600;
    private int frequencyCtcss = 6700;
    private boolean enableCtcss;
    private int volume;
    private int squelch;

    private List<IEventListener> listeners = new ArrayList<>();
}
