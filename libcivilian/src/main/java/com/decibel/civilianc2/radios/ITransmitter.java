package com.decibel.civilianc2.radios;

import java.io.IOException;

/**
 * Created by dburnett on 2/22/2018.
 */

public interface ITransmitter {
    int getTransmitFreq();
    void setTransmitFreq(int transmitFreq) throws IOException;
    void setToneSquelch(int frequency) throws IOException;
    void enableToneSquelch(boolean enable) throws IOException;
    int getToneSquelchFrequency();
    boolean getToneSquelchEnabled();

    ISoftwareTransmitter getSoftwareTransmitter();
}
