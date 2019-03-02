package com.decibel.civilianc2.radios;


import java.io.IOException;

public class RadioController {
    public RadioController(ITransceiver transceiver){
        this.transceiver = transceiver;
        this.band = getBandFromFreq(this.transceiver.getReceiveFreq());
        int rx = this.transceiver.getReceiveFreq();
        int tx = this.transceiver.getTransmitFreq();
        if(rx != tx){
            if(tx > rx)
                this.offset = Offset.Plus;
            else
                this.offset = Offset.Minus;
        }
    }

    public enum Band {
        Unauthorized,
        Band2M,
        Band1_25M,
        Band70CM
    }

    public enum Offset {
        Plus,
        Minus,
        Off
    }

    public Offset getOffset() {
        return offset;
    }

    public void setOffset(Offset newOffset) throws Exception {
        try {
            int freqOffset = getOffsetInMhz(newOffset, this.band);
            int txTargetFreq = transceiver.getReceiveFreq() + freqOffset;
            transceiver.setTransmitFreq(txTargetFreq);
            offset = newOffset;
        }
        catch (Exception e){
            throw e;
        }
    }

    public void setFrequency(int frequency) throws Exception{
        Band targetBand = getBandFromFreq(frequency);
        int txTargetFreq = frequency + getOffsetInMhz(this.getOffset(), targetBand);
        transceiver.setFrequency(frequency, txTargetFreq);
        this.band = targetBand;
    }

    public String getBandString(){
        switch (band){
            case Band2M:return    "2M";
            case Band1_25M:return "220";
            case Band70CM:return  "70cm";
            case Unauthorized: return "---";
        }
        return new String();
    }

    public Band getBand(){return this.band;}

    public void setBand(Band band) throws Exception {
        try {
            switch (band) {
                //default to calling frequencies
                case Band2M:
                    transceiver.setReceiveFreq(146520);
                    transceiver.setTransmitFreq(146520);
                    break;
                case Band1_25M:
                    transceiver.setReceiveFreq(223500);
                    transceiver.setTransmitFreq(223500);
                    break;
                case Band70CM:
                    transceiver.setReceiveFreq(446000);
                    transceiver.setTransmitFreq(446000);
                    break;
            }
            this.band = band;
        }
        catch (Exception e){
            throw e;
        }
    }



    public void setChannel(Channel channel) throws IOException {
        transceiver.setTransmitFreq(channel.getTxFreq());
        transceiver.setReceiveFreq(channel.getRxFreq());
    }

    public void incrementCtcss(boolean up) throws IOException {
        if(up && ctcssIndex < ctcssArray.length - 1){
            ctcssIndex++;
        } else if(!up && ctcssIndex > 0){
            ctcssIndex--;
        }
        transceiver.setToneSquelch((int)(ctcssArray[ctcssIndex] * 100));
    }

    public ITransceiver getTransceiver() {return transceiver;}

    private static int getOffsetInMhz(Offset offset, Band band){
        if(offset == Offset.Off)return 0;
        switch (band) {
            case Band2M:
                if (offset == Offset.Plus) {
                    return Offset2M;
                } else {
                    return -Offset2M;
                }
            case Band1_25M:
                if (offset == Offset.Plus) {
                    return Offset220;
                } else {
                    return -Offset220;
                }
            case Band70CM:
                if (offset == Offset.Plus) {
                    return Offset70CM;
                } else {
                    return -Offset70CM;
                }
        }
        return 0;
    }

    private static Band getBandFromFreq(int freq){
        if(freq >= Min2MFreq && freq <= Max2MFreq){
            return Band.Band2M;
        } else if (freq >= Min220Freq && freq <= Max220Freq){
            return Band.Band1_25M;
        } else if (freq >= Min70cmFreq && freq <= Max70cmFreq ) {
            return Band.Band70CM;
        }
        return Band.Unauthorized;
    }

    private Band band = Band.Band2M;
    private ITransceiver transceiver;
    private Offset offset = Offset.Off;
    private int ctcssIndex = 0;

    private static int Offset2M = 600;
    private static int Offset70CM = 5000;
    private static int Offset220 = 1600;

    private double [] ctcssArray = {
             67.0,  69.3,  71.9,  74.4,  77.0,  79.7,  82.5,  85.4,  88.5,  91.5,  94.8,  97.4, 100.0, 103.5,
            107.2, 110.9, 114.8, 118.8, 123.0, 127.3, 131.8, 136.5, 141.3, 146.2, 150.0, 151.4, 156.7, 159.8,
            162.2, 165.5, 167.9, 171.3, 173.8, 177.3, 179.9, 183.5, 186.2, 189.9, 192.8, 196.6, 199.5, 203.5,
            206.5, 210.7, 213.8, 218.1, 221.3, 225.7, 229.1, 233.6, 237.1, 241.8, 245.5, 250.3, 254.1

    };

    private static final int Min2MFreq = 144000;
    private static final int Max2MFreq = 148000;
    private static final int Min220Freq = 222000;
    private static final int Max220Freq = 225000;
    private static final int Min70cmFreq = 420000;
    private static final int Max70cmFreq = 450000;
}
