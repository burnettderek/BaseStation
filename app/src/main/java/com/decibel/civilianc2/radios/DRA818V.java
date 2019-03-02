package com.decibel.civilianc2.radios;

import com.decibel.civilianc2.model.dataaccess.UserSettings;
import com.decibel.civilianc2.tools.Convert;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dburnett on 1/9/2018.
 */

public class DRA818V implements ITransceiver{

    public String getName(){
        return name;
    }
    protected void write(String data) throws IOException{
        byte[] bytes = Convert.toAscii(data + EndTrans);
        uart.write(bytes, bytes.length);
        //for(IEventListener listener : eventListeners)
            //listener.onRadioInterfaceComms(data, true);
    }

    protected String read(){
        byte[] buffer = new byte[256];
        try {
            int read = uart.read(buffer, 256);
            String result = new String(buffer, 0, read, "UTF-8");
            //for(IEventListener listener : eventListeners)
                //listener.onRadioInterfaceComms(result, false);
            return result;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public void setVolume(int v) throws IOException{
        try {
            int vol = (int) ((v * 0.01) * 7.0) + 1;
            write(SetVolumeCommand + vol);
            Thread.sleep(500);
            String result = read();
            if (!isSuccess(result)) {
                throw new Exception("Error setting volume.");
            }
            this.volume = vol;
            this.settings.setSetting(SettingsVolume, this.volume);
        } catch (Exception e){
            throw new IOException(e);
        }
    }

    public void setSquelch(int s) throws IOException{
        int sql = (int)((s * 0.01) * 8.0);
        String result = setFrequncies(this.transmitFreq, this.receiveFreq, sql);

        if(!isSuccess(result)){
            throw new IOException("Error setting squelch.");
        }
        this.squelch = sql;
        this.settings.setSetting(SettingsSquelch, this.squelch);
    }

    public int getSquelch(){
        return (this.squelch * 100)/8;
    }

    @Override
    public int getSignalLevel() {
        return 0;
    }

    public int getVolume(){
        return (((this.volume  - 1) * 100) / 7);
    }

    public boolean connect() throws Exception{
        try {
            write(ConnectCommand);
            Thread.sleep(500);
            String result = read();

            write("AT+SETFILTER=1,1,1");
            Thread.sleep(500);
            String preEmp = read();

            if(!isSuccess(result)){
                write(ConnectCommand);
                Thread.sleep(500);
                result = read();
                if(!isSuccess(result)){
                    write(ConnectCommand);
                    Thread.sleep(500);
                    result = read();
                    if(!isSuccess(ConnectCommand)){
                        write(ConnectCommand);
                        Thread.sleep(500);
                        result = read();
                        if(!isSuccess(result))
                            throw new Exception("Radio returned error code: " + result);
                    }
                }
            }
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public boolean scan(int freq){
        try {
            write(ScanCommand + String.format("%06.4f", freq));
            Thread.sleep(1500);
            String result = read();
            while(result.length() == 0) {
                Thread.sleep(500);
                result = read();
            }

            this.receiveFreq = freq;
            for(IEventListener listener : eventListeners){
                listener.onFrequencyChanged(this.receiveFreq, this.transmitFreq, this.squelch);
            }

            return result.startsWith("S=0");
        }
        catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    public void setTransmitFreq(int transmitFreq) throws IOException {
        this.transmitFreq = transmitFreq;
        String result = setFrequncies(this.transmitFreq, this.receiveFreq, this.squelch);
        if(!isSuccess(result)){
            throw new IOException("Error setting frequency.");
        }
    }

    @Override
    public void setToneSquelch(int frequency) {

    }

    @Override
    public void enableToneSquelch(boolean enable) {

    }

    @Override
    public int getToneSquelchFrequency() {
        return 0;
    }

    @Override
    public boolean getToneSquelchEnabled() {
        return false;
    }

    @Override
    public ISoftwareTransmitter getSoftwareTransmitter() {
        return null;
    }

    public void setReceiveFreq(int receiveFreq) throws IOException {
        this.receiveFreq = receiveFreq;
        String result = setFrequncies(this.transmitFreq, this.receiveFreq, this.squelch);
        if(!isSuccess(result)){
            throw new IOException("Error setting frequency.");
        }
    }

    public int getTransmitFreq() { return this.transmitFreq; }
    public int getReceiveFreq() { return this.receiveFreq; }

    protected String setFrequncies(int tx, int rx, int sql) throws IOException {
        String command = String.format(SetFrequenciesCommand, tx/1000, rx, sql);
        write(command);

        try {
            Thread.sleep(500);
        }catch (InterruptedException e){}

        String result = read();
        if(isSuccess(result)) {
            settings.setSetting(SettingsTxFreq, tx);
            settings.setSetting(SettingsRxFreq, rx);
            settings.setSetting(SettingsSquelch, sql);
            for (IEventListener listener : eventListeners) {
                listener.onFrequencyChanged(rx, tx, sql);
            }
        }
        return result;
    }

    public static DRA818V create(UserSettings userSettings){
        try {
            PeripheralManager service = PeripheralManager.getInstance();
            UartDevice uart = service.openUartDevice("UART0");
            uart.setBaudrate(DRA818V.BaudRate);
            uart.setDataSize(8);
            uart.setParity(UartDevice.PARITY_NONE);
            uart.setStopBits(1);
            Gpio pd = service.openGpio(PDPin);
            pd.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            pd.setValue(true);
            pd.close();
            DRA818V me = new DRA818V();
            me.pttPin = service.openGpio(PTTPin);
            me.pttPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            me.pttPin.setValue(true);
            me.uart = uart;
            //ptt.close();
            me.settings = userSettings;

            me.receiveFreq = userSettings.getInt(SettingsRxFreq, 146670);
            me.transmitFreq = userSettings.getInt(SettingsTxFreq, 146670);
            me.squelch = userSettings.getInt(SettingsSquelch, 0);
            me.volume = userSettings.getInt(SettingsVolume, 0);


            return me;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    private boolean isSuccess(String result){
        String [] tokens = result.split(":");
        return (tokens.length >= 2 && tokens[1].startsWith("0"));
    }


    /*public void setMode(ITransmitter.Mode txMode){
        try{
            if(txMode == ITransmitter.Mode.TRANSMIT){
                pttPin.setValue(false);
            }
            else {
                pttPin.setValue(true);
            }
            this.mode = mode;
        } catch (IOException e){
            e.printStackTrace();
            return;
        }

    }*/

    public void addEventListener(IEventListener listener){
        this.eventListeners.add(listener);
    }

    public void removeEventListener(IEventListener listener){
        this.eventListeners.remove(listener);
    }

    @Override
    public void setFrequency(int rx, int tx) {

    }


    private int transmitFreq = 145330;
    private int receiveFreq;
    private int squelch = 0;
    private int volume = 0;
    //private ITransmitter.Mode mode;

    private UartDevice uart;
    private Gpio pttPin;
    private List<IEventListener> eventListeners = new ArrayList<>();
    private final static int BaudRate = 9600;
    private final static String EndTrans = "\r\n";
    private final static String FrequencyFormat = "06.4%f";
    private final static String ConnectCommand = "AT+DMOCONNECT";
    private final static String ScanCommand = "S+";
    private final static String SetFrequenciesCommand = "AT+DMOSETGROUP=1,%06.4f,%06.4f,0000,%d,0000";//"AT+DMOSETGROUP=0, %06.4f, %06.4f,0000,1,0000";
    private final static String SuccessfullCommand = "0";
    private final static String PTTPin = "BCM24"; //#9
    private final static String PDPin = "BCM22";
    private final static String HLPin = "BCM23";
    private final static String SetVolumeCommand = "AT+DMOSETVOLUME=";
    private final static String name = "818V";
    private static final int FreqLowerBounds = 134000;
    private static final int FreqUpperBounds = 174000;

    private UserSettings settings;
    private static final String SettingsTxFreq = "Device.Radio.TX";
    private static final String SettingsRxFreq = "Device.Radio.RX";
    private static final String SettingsVolume = "Device.Radio.Volume";
    private static final String SettingsSquelch = "Device.Radio.Squelch";


}
