package com.decibel.civilianc2.modems.fsk;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.decibel.civilianc2.model.managers.MessageInterpreter;
import com.decibel.civilianc2.tools.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import sivantoledo.ax25.Afsk1200Demodulator;
import sivantoledo.ax25.PacketHandler;

/**
 * Created by dburnett on 11/6/2017.
 */

public class Demodulator implements PacketHandler{
    protected Demodulator(int sampleFrequency){
        try {
            this.demodulator = new Afsk1200Demodulator(sampleFrequency, 36, 6, this);
            this.sampleFrequency = sampleFrequency;        }
        catch (Exception e){
            log.log(e);
        }
    }

    public void demodulate(Thread thread){
        try {
            this.recorder.startRecording();
            int recordingState = recorder.getRecordingState();
            while (!thread.isInterrupted() && recorder.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
                int count = recorder.read(sampleBuffer, 0, BUFFER_SIZE);
                for(int i = 0; i < count - 1; i++)
                    targetBuffer[i] = ((float)sampleBuffer[i] / MagicScalar);
                demodulator.addSamples(targetBuffer, count); //output appears in PacketHandler passed in ctor
                synchronized (lock) {
                    for (IDemodulatorListener listener : listeners)
                        listener.onAudioLevelChanged(demodulator.peak());
                }
            }
        }
        catch (Exception e){
            log.log(e);
        }
    }
    public void startListeningToMic(){
        log.log(Logger.Level.INFO, "Started listening to audio.");

        this.recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleFrequency, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                4 * BUFFER_SIZE);
        demodThread = new Thread(new Runnable() {
            @Override
            public void run() {
                demodulate(demodThread);
            }
        });
        demodThread.setPriority(Thread.MAX_PRIORITY);
        demodThread.start();
        running = true;
        for(IDemodulatorListener listener : listeners){
            listener.onRunningChanged(running);
        }
    }

    public void stopListeningToMic(){
        log.log(Logger.Level.INFO, "Stopped listening to audio.");
        try {
            demodThread.interrupt();
            demodThread.join(50);
            recorder.stop();
            recorder.release();
            demodThread = null;
            recorder = null;
            running = false;
            for(IDemodulatorListener listener : listeners){
                listener.onRunningChanged(running);
            }
        }
        catch (Exception e){
            log.log(e);
        }
    }

    @Override
    public void handlePacket(byte[] bytes) {
        log.log(Logger.Level.INFO, "Decoded an AX25 packet.");
        synchronized (lock) {
            for (IDemodulatorListener listener : listeners) {
                listener.onPacketReceived(bytes);
            }
        }
    }

    public static Demodulator getInstance(){
        synchronized (lock) {
            if (instance == null) {
                instance = new Demodulator(44100);
                instance.log.log(Logger.Level.DEBUG, "Created new demodulator.");
            }
            return instance;
        }
    }

    public void addListener(IDemodulatorListener listener){
        log.log(Logger.Level.DEBUG, "Adding a listener : " + listener);
        synchronized (lock) {
            listeners.add(listener);
        }
    }

    public void removeListener(IDemodulatorListener listener){
        log.log(Logger.Level.DEBUG, "Removing a listener : " + listener);
        synchronized (lock) {
            listeners.remove(listener);
        }
    }

    public boolean isRunning(){
        return running;
    }

    private Afsk1200Demodulator demodulator;
    private Thread demodThread;
    private AudioRecord recorder;
    private int sampleFrequency;
    private static final int BUFFER_SIZE = 8192;
    private short[] sampleBuffer = new short[BUFFER_SIZE];
    private float[] targetBuffer = new float[BUFFER_SIZE];
    private float MagicScalar = 32768.0f;
    private boolean running = false;
    private List<IDemodulatorListener> listeners = new ArrayList<>();

    private static Demodulator instance;
    private static Object lock = new Object();
    private final Logger<Demodulator> log = new Logger<>(this);
}
