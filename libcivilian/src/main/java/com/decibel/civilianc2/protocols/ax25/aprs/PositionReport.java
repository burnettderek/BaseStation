package com.decibel.civilianc2.protocols.ax25.aprs;

import com.decibel.civilianc2.model.entities.Position;

/**
 * Created by dburnett on 1/4/2018.
 */

public class PositionReport extends APRSPositionMessage {

    @Override
    public String toXML(String linePrefix) {
        StringBuilder builder = new StringBuilder();
        builder.append(linePrefix + "<PositionReport>\n");
        if(Position != null) {
            builder.append(linePrefix + "\t<Position>\n");
            builder.append(linePrefix + "\t\t<Latitude>" + Position.getLatitude() + "</Latitude>\n");
            builder.append(linePrefix + "\t\t<Longitude>" + Position.getLongitude() + "</Longitude>\n");
            if (Position.getAmbiguity() != null)
                builder.append(linePrefix + "\t\t<Ambiguity>" + Position.getAmbiguity() + "</Ambiguity>\n");
            builder.append(linePrefix + "\t</Position>\n");
        }
        builder.append(linePrefix + "</PositionReport>\n");
        return builder.toString();
    }

    @Override
    public String toAX25Text(){
        long ambiguity = 0;
        if(this.Position.getAmbiguity() != null) {
            ambiguity = Math.round(this.Position.getAmbiguity());
        }
        String data = APRSMessage.PacketTypeIndicator.PositionWithMessaging + PositionToString(this.Position, this.Symbol.getSymbolTable(), this.Symbol.getASCII(), (int)ambiguity);
        if(this.Comment != null && !this.Comment.isEmpty())
            data += this.Comment;
        return data;
    }

    public static String getDMS(double decimalDegree, boolean isLatitude, int positionAmbiguity) {
        int minFrac = (int)Math.round(decimalDegree*6000); ///< degree in 1/100s of a minute
        boolean negative = (minFrac < 0);
        if (negative)
            minFrac = -minFrac;
        int deg = minFrac / 6000;
        int min = (minFrac / 100) % 60;
        minFrac = minFrac % 100;
        String ambiguousFrac;

        switch (positionAmbiguity) {
            case 1: // "dd  .  N"
                ambiguousFrac = "  .  "; break;
            case 2: // "ddm .  N"
                ambiguousFrac = String.format("%d .  ", min/10); break;
            case 3: // "ddmm.  N"
                ambiguousFrac = String.format("%02d.  ", min); break;
            case 4: // "ddmm.f N"
                ambiguousFrac = String.format("%02d.%d ", min, minFrac/10); break;
            default: // "ddmm.ffN"
                ambiguousFrac = String.format("%02d.%02d", min, minFrac); break;
        }
        if ( isLatitude ) {
            return String.format("%02d%s%s", deg, ambiguousFrac, ( negative ? "S" : "N"));
        } else {
            return String.format("%03d%s%s", deg, ambiguousFrac, ( negative ? "W" : "E"));
        }
    }

    public static String PositionToString(Position position, String symbolTable, char symbolCode, int ambiguity) {
        return getDMS(position.getLatitude(),true, ambiguity)+symbolTable+getDMS(position.getLongitude(),false, ambiguity)+symbolCode;
    }
}
