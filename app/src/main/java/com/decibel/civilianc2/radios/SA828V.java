package com.decibel.civilianc2.radios;

import com.decibel.civilianc2.model.dataaccess.UserSettings;
import com.decibel.civilianc2.tools.Convert;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dburnett on 4/4/2018.
 */

public class SA828V implements ITransceiver{

    public String getName(){
        return name.replace("_VER", " v");
    }

    protected void write(String data) throws IOException {
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
        int vol = (int)((v * 0.01) * 7.0) + 1;
        write(SetVolumeCommand + vol);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        String result = read();
        if(!isSuccess(result)){
            throw new IOException("Error setting volume.");
        }
        this.volume = vol;
        this.settings.setSetting(SettingsVolume, this.volume);
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

            //String setGroup = "AAFA3162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,000,000,0";
            //write(setGroup);
            //Thread.sleep(500);
            //result = read();
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
            this.name = result;
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
        if(channelAlternator) {
            String command = String.format(SetFrequenciesChannel1, tx/1000, rx/1000, sql);
            write(command);
        } else {
            String command = String.format(SetFrequenciesChannel0, tx/1000, rx/1000, sql);
            write(command);
        }

        try {
            Thread.sleep(500);
        }catch (InterruptedException e){}

        String result = read();

        if(isSuccess(result)) {
            channelAlternator = !channelAlternator;
            channelBit1Pin.setValue(channelAlternator); //force a channel switch
            settings.setSetting(SettingsTxFreq, tx);
            settings.setSetting(SettingsRxFreq, rx);
            settings.setSetting(SettingsSquelch, sql);
            for (IEventListener listener : eventListeners) {
                listener.onFrequencyChanged(rx, tx, sql);
            }
        }
        return result;
    }

    public static SA828V create(UserSettings userSettings){
        try {
            PeripheralManager service = PeripheralManager.getInstance();
            UartDevice uart = service.openUartDevice("UART0");
            uart.setBaudrate(SA828V.BaudRate);
            uart.setDataSize(8);
            uart.setParity(UartDevice.PARITY_NONE);
            uart.setStopBits(1);
            //Gpio pd = service.openGpio(PDPin);
            //pd.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            //pd.setValue(true);
            //pd.close();
            SA828V me = new SA828V();
            me.pttPin = service.openGpio(PTTPin);
            me.pttPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            me.pttPin.setValue(true);
            me.channelBit1Pin = service.openGpio(ChannelBit1);
            me.channelBit1Pin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            me.channelBit1Pin.setValue(false);
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
        return !result.contains("ERROR");
    }

    public void setChannel(Channel channel) throws IOException{
        this.transmitFreq = channel.getTxFreq();
        this.receiveFreq = channel.getRxFreq();
        setFrequncies(this.transmitFreq, this.receiveFreq, this.squelch);
    }


    @Override
    public ISoftwareTransmitter getSoftwareTransmitter(){
        return null;
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
    private String name = "Unknown device";
    private boolean channelAlternator = false;

    private UartDevice uart;
    private Gpio pttPin;
    private Gpio channelBit1Pin;
    private List<IEventListener> eventListeners = new ArrayList<>();
    private final static int BaudRate = 9600;
    private final static String EndTrans = "\r\n";
    private final static String FrequencyFormat = "06.4%f";
    private final static String ConnectCommand = "AAFAA";
    private final static String ScanCommand = "S+";
    private final static String SetFrequenciesChannel0 = "AAFA3%06.4f,%06.04f,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,000,000,%d";
    private final static String SetFrequenciesChannel1 = "AAFA3162.4500,162.4500,%06.4f,%06.04f,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,162.4500,000,000,%d";
    private final static String SuccessfullCommand = "0";
    private final static String PTTPin = "BCM23"; //#9
    private final static String ChannelBit1 = "BCM24";

    private final static String SetVolumeCommand = "AT+DMOSETVOLUME=";

    private static final double FreqLowerBounds = 134.000;
    private static final double FreqUpperBounds = 174.000;

    private UserSettings settings;
    private static final String SettingsTxFreq = "Device.Radio.TX";
    private static final String SettingsRxFreq = "Device.Radio.RX";
    private static final String SettingsVolume = "Device.Radio.Volume";
    private static final String SettingsSquelch = "Device.Radio.Squelch";


}
