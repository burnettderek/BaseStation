package com.decibel.civilianc2.radios;

import java.io.IOException;

/**
 * Created by dburnett on 2/22/2018.
 */

public interface ITransceiver extends IReceiver, ITransmitter{
    void setFrequency(int rx, int tx) throws IOException;


}
