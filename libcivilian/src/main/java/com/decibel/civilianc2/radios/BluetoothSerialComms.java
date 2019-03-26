package com.decibel.civilianc2.radios;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class BluetoothSerialComms implements ISerialComms {
    public BluetoothSerialComms(BluetoothSocket socket) throws IOException{
        out = socket.getOutputStream();
        in = socket.getInputStream();
        listenThread = new Thread(){
            public void run(){
                StringBuilder currentResponse = new StringBuilder();
                byte[] buffer = new byte[256];
                while(true){
                    try {
                        synchronized (initializationLock) {
                            initializationLock.notify();
                        }
                        int length = in.read(buffer);
                        currentResponse.append(new String(buffer, 0, length));
                        if(buffer[length - 1] == Delimiter){
                            synchronized (sendLock) {
                                response = currentResponse.toString();
                                waiting = false;
                                sendLock.notify();
                            }
                            currentResponse = new StringBuilder();
                        }
                    } catch (IOException e) {
                        break;
                    }
                }
            }
        };
        listenThread.start();
        synchronized (initializationLock){ //let's block till the listening thread is going.
            try {
                initializationLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void setListener(ISerialCommsListener listener) {
        this.listener = listener;
    }

    @Override
    public void send(String message) throws IOException {
        synchronized (sendLock) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            out.write((message + Delimiter).getBytes());
        }
        if(listener != null)
            listener.onComms(message, true);
    }

    @Override
    public String sendCommand(String message) throws IOException {
        try {
            Thread.sleep(100);
            String result = null;
            synchronized (sendLock) {
                out.write((message + Delimiter).getBytes());
                waiting = true;
                sendLock.wait(500);
                if(waiting)
                    throw new IOException("Lost connection with serial device.");
                result = this.response;
                response = null;
            }
            if(listener != null) {
                listener.onComms(message, true);
                listener.onComms(result, false);
            }

            return result;
        } catch (InterruptedException e){
            throw new IOException(e);
        }
    }

    private OutputStream out;
    private InputStream in;
    private String response = null;
    //private byte[] buffer = new byte[256];
    private static final char Delimiter = '\r';
    private ISerialCommsListener listener;
    private Thread listenThread;
    private Object sendLock = new Object();
    private Object initializationLock = new Object();
    private boolean waiting = false;
}
