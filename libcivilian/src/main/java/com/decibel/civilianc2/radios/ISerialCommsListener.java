package com.decibel.civilianc2.radios;

public interface ISerialCommsListener {
    void onComms(String message, boolean transmitted);
}
