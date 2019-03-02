package com.decibel.civilianc2.tools;

import java.io.UnsupportedEncodingException;

/**
 * Created by dburnett on 1/9/2018.
 */

public final class Convert {
    public static byte[] toAscii(String string){
        try{
            byte[] bytes = string.getBytes("UTF-8");
            return bytes;
        }
        catch (UnsupportedEncodingException e){
            return null;
        }
    }

    public static String toFrequencyString(int freq){
        return toFrequencyString(freq, null);
    }

    public static String toFrequencyString(int freq, String suffix){
        if(suffix == null)
            return String.format("%6.3f", freq / 1000.0);
        else
            return String.format("%6.3f", freq / 1000.0) + suffix;
    }
}
