package com.decibel.civilianc2.radios;

import java.io.IOException;

public interface ISoftwareTransmitter {
    enum Mode {
        TRANSMIT,
        RECEIVE
    }

    void setMode(Mode mode) throws IOException;
}
