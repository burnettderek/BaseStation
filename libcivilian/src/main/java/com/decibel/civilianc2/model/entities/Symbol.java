package com.decibel.civilianc2.model.entities;

/**
 * Created by dburnett on 12/28/2017.
 */

public class Symbol {
    public Symbol(String table, int symbolIndex){
        this.symbolTable = table;
        this.symbolIndex = symbolIndex;
    }

    public int getSymbolIndex(){
        return this.symbolIndex;
    }

    public char getASCII(){
        return (char)(this.symbolIndex + ASCII_SYMBOL_OFFSET);
    }

    public String getSymbolTable() {
        return this.symbolTable;
    }


    public final static String PRIMARY_SYMBOL_TABLE = "/";
    /*public final static char ALTERNATE_SYMBOL_TABLE = '\\';
    public final static char ALTENATE_SYMBOL_OVERLAY_0 = '0';
    public final static char ALTENATE_SYMBOL_OVERLAY_1 = '1';
    public final static char ALTENATE_SYMBOL_OVERLAY_2 = '2';
    public final static char ALTENATE_SYMBOL_OVERLAY_3 = '3';
    public final static char ALTENATE_SYMBOL_OVERLAY_4 = '4';
    public final static char ALTENATE_SYMBOL_OVERLAY_5 = '5';
    public final static char ALTENATE_SYMBOL_OVERLAY_6 = '6';
    public final static char ALTENATE_SYMBOL_OVERLAY_7 = '7';
    public final static char ALTENATE_SYMBOL_OVERLAY_8 = '8';
    public final static char ALTENATE_SYMBOL_OVERLAY_9 = '9';
    public final static char ALTENATE_SYMBOL_OVERLAY_A = 'A';
    public final static char ALTENATE_SYMBOL_OVERLAY_B = 'B';
    public final static char ALTENATE_SYMBOL_OVERLAY_C = 'C';
    public final static char ALTENATE_SYMBOL_OVERLAY_D = 'D';
    public final static char ALTENATE_SYMBOL_OVERLAY_E = 'E';
    public final static char ALTENATE_SYMBOL_OVERLAY_F = 'F';
    public final static char ALTENATE_SYMBOL_OVERLAY_G = 'G';
    public final static char ALTENATE_SYMBOL_OVERLAY_H = 'H';
    public final static char ALTENATE_SYMBOL_OVERLAY_I = 'I';
    public final static char ALTENATE_SYMBOL_OVERLAY_J = 'J';
    public final static char ALTENATE_SYMBOL_OVERLAY_EXCLAMATION = '!';
    public final static char ALTENATE_SYMBOL_OVERLAY_ASTERISK = '*';*/



    public static int getSymbolIndex(char asciiValue){
        int intVal = (int)asciiValue;
        intVal -= ASCII_SYMBOL_OFFSET;
        return intVal;
    }

    private String symbolTable;
    private int symbolIndex;
    private static final int ASCII_SYMBOL_OFFSET = 33;
}
