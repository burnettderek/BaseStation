package com.decibel.civilianc2.model.entities;

/**
 * Created by dburnett on 4/17/2018.
 */

public class Item {
    public Item(String name, Symbol symbol){
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    private String name;
    private Symbol symbol;

}
