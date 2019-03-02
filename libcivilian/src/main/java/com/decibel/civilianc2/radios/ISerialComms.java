package com.decibel.civilianc2.radios;

import java.io.IOException;

public interface ISerialComms {
    void setListener(ISerialCommsListener listener);
    void send(String message) throws IOException;
    String sendCommand(String message) throws IOException;
}
