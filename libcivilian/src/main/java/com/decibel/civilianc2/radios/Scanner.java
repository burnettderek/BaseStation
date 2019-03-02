package com.decibel.civilianc2.radios;


import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by dburnett on 2/28/2018.
 */

public class Scanner {
    public Scanner(Context context, IReceiver receiver, int freqStep, int startFreq, int endFreq){
        this.receiver = receiver;
        this.freqStep = freqStep;
        this.startFreq = startFreq;
        this.endFreq = endFreq;
        this.context = context;
    }

    public void start(){
        try {
            receiver.setReceiveFreq(this.startFreq);
        } catch (Exception e) {
            Toast.makeText(context.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean foundSignal = false;
                if(!paused) {
                    int currentFreq = receiver.getReceiveFreq();

                    if (currentFreq + freqStep > endFreq) {
                        try {
                            foundSignal = receiver.scan(startFreq);
                        } catch (Exception e) {
                            Toast.makeText(context.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            return; //just stop
                        }
                    } else {
                        try {
                            foundSignal = receiver.scan(currentFreq + freqStep);
                        } catch (Exception e) {
                            Toast.makeText(context.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            return; //just stop
                        }
                    }
                }
                if(!stopped) {
                    if (foundSignal)
                        handler.postDelayed(this, ListenPeriodInMs);
                    else
                        handler.postDelayed(this, SleepPeriodInMs);
                } else {            //we were stopped. Reset everything
                    stopped = true; //reset stop
                    paused = true;
                }
            }
        }, SleepPeriodInMs);
    }

    public void setPaused(boolean paused){
        this.paused = paused;
    }

    public boolean getPaused(){
        return this.paused;
    }

    public void stop(){
        this.paused = true;
        this.stopped = true;
    }

    public boolean getStopped(){
        return this.stopped;
    }

    private IReceiver receiver;
    private int freqStep;
    private int startFreq;
    private int endFreq;
    private Context context;

    private Handler handler = new Handler();
    private static final int SleepPeriodInMs = 500;
    private static final int ListenPeriodInMs = 10000;
    private boolean paused = false;
    private boolean stopped = false;
}
