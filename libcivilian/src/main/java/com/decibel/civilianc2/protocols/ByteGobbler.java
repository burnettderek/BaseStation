package com.decibel.civilianc2.protocols;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dburnett on 1/3/2018.
 */

public class ByteGobbler {
    public ByteGobbler(byte[] lunch){
        this.lunch = lunch;
    }

    public byte peek(){
        return this.lunch[currentPosition];
    }

    public byte peek(int offset){
        return this.lunch[currentPosition + offset];
    }

    public byte munch(){
        byte result = this.lunch[currentPosition];
        currentPosition++;
        return result;
    }

    public byte[] munch(int length){
        if(currentPosition + length > lunch.length){
            length = lunch.length - currentPosition;
        }
        byte[] results = Arrays.copyOfRange(lunch, currentPosition, currentPosition + length);
        currentPosition += length;
        return results;
    }

    public byte[] munch(byte delimiter){
        for(int i = currentPosition; i < lunch.length; i++){
            if(lunch[i] == delimiter)return munch(i - currentPosition);
        }
        return null; //no delimiter found
    }

    public byte[] devour(){
        return Arrays.copyOfRange(lunch, currentPosition, lunch.length);
    }

    public void burn(int bytes){
        if(currentPosition + bytes < lunch.length){
            currentPosition += bytes;
        }
        else{
            currentPosition = lunch.length - 1;
        }
    }

    public int remaining(){
        return this.lunch.length - currentPosition;
    }



    private byte[] lunch;
    private int currentPosition;
}
