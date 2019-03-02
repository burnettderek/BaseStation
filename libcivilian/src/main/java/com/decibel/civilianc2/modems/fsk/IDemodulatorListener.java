package com.decibel.civilianc2.modems.fsk;

/**
 * Created by dburnett on 12/29/2017.
 */

public interface IDemodulatorListener {
    void onAudioLevelChanged(int audioLevel);
    void onPacketReceived(byte[] bytes);
    void onRunningChanged(boolean running);
}
