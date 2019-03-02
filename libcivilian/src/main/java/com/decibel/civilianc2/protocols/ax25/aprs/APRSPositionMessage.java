package com.decibel.civilianc2.protocols.ax25.aprs;

import com.decibel.civilianc2.model.entities.Position;
import com.decibel.civilianc2.model.entities.Symbol;

/**
 * Created by dburnett on 1/8/2018.
 */

public class APRSPositionMessage extends APRSMessage {
    public Position Position;
    public Symbol Symbol;
    public String Comment;
}
