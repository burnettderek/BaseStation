package com.decibel.civilianc2.radios;

import java.io.IOException;

/**
 * Created by dburnett on 2/22/2018.
 */

public interface IReceiver {

    String getName();

    interface IEventListener {
        void onFrequencyChanged(int rx, int tx, int sql);
    }

    void setReceiveFreq(int receiveFreq) throws IOException;
    int getReceiveFreq();
    int getVolume();
    void setVolume(int v) throws IOException;
    void setSquelch(int s) throws IOException;
    int getSquelch();
    int getSignalLevel();
    boolean scan(int freq) throws IOException;

    boolean isConnected() throws IOException;

    void addEventListener(IEventListener listener);
    void removeEventListener(IEventListener listener);
}
