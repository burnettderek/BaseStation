package com.decibel.civilianc2.radios;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.IOException;

public class UsbSerialComms implements ISerialComms {
    public UsbSerialComms(UsbSerialDevice device){
        this.serialDevice = device;
        this.serialDevice.read(mCallback);
    }

    @Override
    public void send(String message) throws IOException {
        message += Delimiter;
        synchronized (sendLock) {
            this.serialDevice.write(message.getBytes());
        }
        if(listener != null)listener.onComms(message, true);
    }

    @Override
    public String sendCommand(String message) throws IOException {
        try {
            Thread.sleep(250);
            String transmission = message + Delimiter;
            this.serialDevice.write(transmission.getBytes());
            if (listener != null) listener.onComms(transmission, true);
            synchronized (sendLock) {
                waiting = true;
                sendLock.wait(5000);
                if(waiting)
                    throw new IOException("Lost connection with serial device.");
                String result = rxBuffer;
                rxBuffer = null;
                return result;
            }
        } catch (InterruptedException e){
            throw new IOException(e);
        }
    }

    @Override
    public void setListener(ISerialCommsListener listener){
        this.listener = listener;
    }

    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] bytes) {
            synchronized (sendLock){
                rxBuffer += new String(bytes);
                if(rxBuffer.charAt(rxBuffer.length() - 1) == Delimiter) {
                    if(rxBuffer.startsWith("null"))
                        rxBuffer = rxBuffer.substring(4);
                    if(listener != null)
                        listener.onComms(rxBuffer, false);
                    waiting = false;
                    sendLock.notify();
                }
            }
        }
    };

    private String rxBuffer;
    private UsbSerialDevice serialDevice;
    private Object sendLock = new Object();
    private static final char Delimiter = '\r';
    private ISerialCommsListener listener;
    private boolean waiting = false;
}
