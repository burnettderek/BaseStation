package com.decibel.civilianc2.protocols.ax25.aprs;

import com.decibel.civilianc2.model.entities.Position;

/**
 * Created by dburnett on 1/4/2018.
 */

public class ObjectDefinition extends APRSPositionMessage {
    public String Name;
    public Boolean Live;

    @Override
    public String toXML(String linePrefix) {
        StringBuilder builder = new StringBuilder();
        builder.append(linePrefix + "<ObjectDefinition>\n");
        if(Name != null)
            builder.append(linePrefix + "\t<Name>" + Name + "</Name>\n");
        if(Live != null)
            builder.append(linePrefix + "\t<Live>" + (Live ? "true" : "false") + "</Live>\n");
        if(Position != null) {
            builder.append(linePrefix + "\t<Position>\n");
            builder.append(linePrefix + "\t\t<Latitude>" + Position.getLatitude() + "</Latitude>\n");
            builder.append(linePrefix + "\t\t<Longitude>" + Position.getLongitude() + "</Longitude>\n");
            if (Position.getAmbiguity() != null)
                builder.append(linePrefix + "\t\t<Ambiguity>" + Position.getAmbiguity() + "</Ambiguity>\n");
            builder.append(linePrefix + "\t</Position>\n");
        }
        if(Comment != null){
            builder.append(linePrefix + "\t<Comment>" + Comment + "</Comment>\n");
        }
        builder.append(linePrefix + "</ObjectDefinition>\n");
        return builder.toString();
    }
}
