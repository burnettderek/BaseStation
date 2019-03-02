package com.decibel.civilianc2.protocols.ax25.aprs;

import com.decibel.civilianc2.model.entities.Position;
import com.decibel.civilianc2.model.entities.Symbol;
import com.decibel.civilianc2.protocols.ByteGobbler;

import java.io.UnsupportedEncodingException;

/**
 * Created by dburnett on 1/5/2018.
 */

public class APRSDecoder {
    public APRSMessage decode(byte[] bytes, final String destination){
        ByteGobbler gobbler = new ByteGobbler(bytes);
        byte dataType = gobbler.munch();
        switch (dataType) {
            case APRSMessage.PacketTypeIndicator.PositionNoMessaging:
            case APRSMessage.PacketTypeIndicator.PositionWithMessaging:
            case APRSMessage.PacketTypeIndicator.PositionWithTimestampNoMessage:
            case APRSMessage.PacketTypeIndicator.PositionWithTimestampWithMessage:
            case APRSMessage.PacketTypeIndicator.PositionCurrentMicEData:
            case APRSMessage.PacketTypeIndicator.SpecUnusedPositionReport:
            case APRSMessage.PacketTypeIndicator.PositionRawGPS:
            case APRSMessage.PacketTypeIndicator.PositionOldMicEData:
                return decodePositionReport(gobbler, dataType, destination);

            case APRSMessage.PacketTypeIndicator.ObjectDefinition:
                return decodeObjectDefinition(gobbler);

            case APRSMessage.PacketTypeIndicator.ItemDefinition:
                return decodeItemDefinition(gobbler);

            case APRSMessage.PacketTypeIndicator.TextMessage:
                return decodeTextMessage(gobbler);

            case APRSMessage.PacketTypeIndicator.Status:
                return decodeStatusMessage(gobbler);

            default:
                return new APRSMessage(dataType);
        }
    }

    private PositionReport decodePositionReport(ByteGobbler gobbler, Byte dataType, final String destination){
        if(dataType == APRSMessage.PacketTypeIndicator.PositionWithTimestampNoMessage || dataType == APRSMessage.PacketTypeIndicator.PositionWithTimestampWithMessage){
            gobbler.burn(TimeStampLength);
        }
        PositionReport report = new PositionReport();
        if(dataType == APRSMessage.PacketTypeIndicator.PositionWithTimestampWithMessage || dataType == APRSMessage.PacketTypeIndicator.PositionWithTimestampNoMessage
                || dataType == APRSMessage.PacketTypeIndicator.PositionWithMessaging || dataType == APRSMessage.PacketTypeIndicator.PositionNoMessaging) {
            decodePositionAndSymbol(gobbler, report);
        }

        if(dataType == APRSMessage.PacketTypeIndicator.PositionCurrentMicEData || dataType == APRSMessage.PacketTypeIndicator.PositionOldMicEData){
            String sympbolTable = new String() + gobbler.peek(8);
            byte index = gobbler.peek(7);
            report.Symbol = new Symbol(sympbolTable, Symbol.getSymbolIndex((char)index));
            try {
                report.Position = APRSDecoder.parseMICe(gobbler.devour(), destination);
            }
            catch (Exception e){ }
        }

        if(report.Position == null){
            String result = report.toXML("");
        }
        return report;
    }

    private ObjectDefinition decodeObjectDefinition(ByteGobbler gobbler){
        ObjectDefinition result = new ObjectDefinition();
        result.Name = new String(gobbler.munch(9)).trim();
        result.Live = (gobbler.munch() == '*');
        gobbler.munch(7);
        decodePositionAndSymbol(gobbler, result);

        return result;
    }

    private void decodePositionAndSymbol(ByteGobbler gobbler, APRSPositionMessage message) {
        byte posByte = gobbler.peek();
        if(posByte > '0' && posByte < '9') { //is it an ascii numeral?
            try {
                String table = new String() + (char)gobbler.peek(8);
                byte index = gobbler.peek(18);
                message.Symbol = new Symbol(table, Symbol.getSymbolIndex((char)index));
                message.Position = parseUncompressed(gobbler.munch(19));
            }
            catch (Exception e){ /*Well... he just doesn't get a position then.*/ }
        }
        else{
            String table = new String() + gobbler.peek(0);
            byte index = gobbler.peek(9);
            message.Symbol = new Symbol(table, Symbol.getSymbolIndex((char)index));
            message.Position = parseCompressed(gobbler.munch(12));
        }
        if(gobbler.remaining() > 0){
            message.Comment = new String(gobbler.devour());
        }
    }

    private TextMessage decodeTextMessage(ByteGobbler gobbler){
        TextMessage textMessage = new TextMessage();
        String text = new String(gobbler.devour());
        int index = text.indexOf('{');
        if(index > 0){
            textMessage.Message = text.substring(0, index);
            textMessage.MessageId = text.substring(index + 1, text.length() - 1);
            textMessage.Acknowledge = true;
        }
        else{
            textMessage.Message = text;
            textMessage.Acknowledge = false;
        }
        return textMessage;
    }

    private StatusMessage decodeStatusMessage(ByteGobbler gobbler){
        StatusMessage message = new StatusMessage();
        message.Status = new String(gobbler.devour());
        return message;
    }

    private ItemDefinition decodeItemDefinition(ByteGobbler gobbler){
        return new ItemDefinition();
    }


    private Position parseUncompressed(byte[] bytes) throws Exception {
        if (bytes[0] == '/' || bytes[0] == '@') {
            throw new UnsupportedEncodingException();
        }

        int positionAmbiguity = 0;
        char[] posbuf = new char[bytes.length + 1];
        int pos = 0;
        for (int i = 0; i < bytes.length; i++) {
            posbuf[pos] = (char) bytes[i];
            pos++;
        }
        // latitude
        if (posbuf[2] == ' ') {
            posbuf[2] = '3';
            posbuf[3] = '0';
            posbuf[5] = '0';
            posbuf[6] = '0';
            positionAmbiguity = 1;
        }
        if (posbuf[3] == ' ') {
            posbuf[3] = '5';
            posbuf[5] = '0';
            posbuf[6] = '0';
            positionAmbiguity = 2;
        }
        if (posbuf[5] == ' ') {
            posbuf[5] = '5';
            posbuf[6] = '0';
            positionAmbiguity = 3;
        }
        if (posbuf[6] == ' ') {
            posbuf[6] = '5';
            positionAmbiguity = 4;
        }
        // longitude
        if (posbuf[12] == ' ') {
            posbuf[12] = '3';
            posbuf[13] = '0';
            posbuf[15] = '0';
            posbuf[16] = '0';
            positionAmbiguity = 1;
        }
        if (posbuf[13] == ' ') {
            posbuf[13] = '5';
            posbuf[15] = '0';
            posbuf[16] = '0';
            positionAmbiguity = 2;
        }
        if (posbuf[15] == ' ') {
            posbuf[15] = '5';
            posbuf[16] = '0';
            positionAmbiguity = 3;
        }
        if (posbuf[16] == ' ') {
            posbuf[16] = '5';
            positionAmbiguity = 4;
        }

        try {
            double latitude = parseDegMin(posbuf, 0, 2, 7, true);
            char lath = (char) posbuf[7];
            char symbolTable = (char) posbuf[8];
            double longitude = parseDegMin(posbuf, 9, 3, 8, true);
            char lngh = (char) posbuf[17];
            char symbolCode = (char) posbuf[18];

            if (lath == 's' || lath == 'S')
                latitude = 0.0F - latitude;
            else if (lath != 'n' && lath != 'N')
                throw new Exception("Bad latitude sign character");

            if (lngh == 'w' || lngh == 'W')
                longitude = 0.0F - longitude;
            else if (lngh != 'e' && lngh != 'E')
                throw new Exception("Bad longitude sign character");
            return new Position(latitude, longitude, positionAmbiguity);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    private Position parseCompressed(byte[] bytes){
        int lat1 = (char) bytes[1] - 33;
        int lat2 = (char) bytes[2] - 33;
        int lat3 = (char) bytes[3] - 33;
        int lat4 = (char) bytes[4] - 33;

        int lng1 = (char) bytes[5] - 33;
        int lng2 = (char) bytes[6] - 33;
        int lng3 = (char) bytes[7] - 33;
        int lng4 = (char) bytes[8] - 33;

        double lat = 90.0 - ((float) (lat1 * 91 * 91 * 91 + lat2 * 91 * 91 + lat3 * 91 + lat4) / 380926.0);
        double lng = -180.0 + ((float) (lng1 * 91 * 91 * 91 + lng2 * 91 * 91 + lng3 * 91 + lng4) / 190463.0);
        Position result = new Position(lat, lng);
        return result;
    }

    private static double parseDegMin(char[] txt, int cursor, int degSize, int len, boolean decimalDot)
            throws Exception {
        //System.out.println("DegMin data is "+new String(txt));
        if (txt == null || txt.length < cursor + degSize + 2)
            throw new Exception("Too short degmin data");
        double result = 0.0F;
        for (int i = 0; i < degSize; ++i) {
            char c = txt[cursor + i];
            if (c < '0' || c > '9')
                throw new Exception("Bad input decimals:  " + c);
            result = result * 10.0F + (c - '0');
        }
        double minFactor = 10.0F; // minutes factor, divide by 10.0F for every
        // minute digit
        double minutes = 0.0F;
        int mLen = txt.length - degSize - cursor;
        if (mLen > len - degSize)
            mLen = len - degSize;
        for (int i = 0; i < mLen; ++i) {
            char c = txt[cursor + degSize + i];
            if (decimalDot && i == 2) {
                if (c == '.')
                    continue; // Skip it! (but only at this position)
                throw new Exception("Expected decimal dot");
            }
            if (c < '0' || c > '9')
                throw new Exception("Bad input decimals: " + c);
            minutes += minFactor * (c - '0');
            minFactor *= 0.1D;
        }
        if (minutes >= 60.0D)
            throw new Exception("Bad minutes value - 60.0 or over");
        // return result
        result += minutes / 60.0D;
        result = Math.round(result * 100000.0) * 0.00001D;
        if (degSize == 2 && result > 90.01D)
            throw new Exception("Latitude value too high");
        if (degSize == 3 && result > 180.01F)
            throw new Exception("Longitude value too high");
        return result;
    }

    private static Position parseMICe(byte[] msgBody, final String destinationCall) throws Exception {
        // Check that the destination call exists and is
        // of the right size for mic-e
        // System.out.print("MICe:");
        String dcall = destinationCall;
        if (destinationCall.indexOf("-") > -1) {
            dcall = destinationCall.substring(0, destinationCall.indexOf("-"));
        }
        if (dcall.length() != 6) {
            throw new Exception("MicE Destination Call incorrect length:  " + dcall);
        }
        // Validate destination call
        char[] destcall = dcall.toCharArray();
        for (int i = 0; i < 3; ++i) {
            char c = destcall[i + 1];
            if (!(('0' <= c && c <= '9') || ('A' <= c && c <= 'L') || ('P' <= c && c <= 'Z'))) {
                throw new Exception("Digit " + i + " dorked:  " + c);
            }
        }
        for (int i = 3; i < 5; ++i) {
            char c = destcall[i + 1];
            if (!(('0' <= c && c <= '9') || ('L' == c) || ('P' <= c && c <= 'Z'))) {
                throw new Exception("Digit " + i + " dorked:  " + c);
            }
        }
        // dstcall format check acceptable
        char c = (char) msgBody[1 + 0];
        if (c < '\u0026' || c > '\u007f') {
            throw new Exception("Raw packet contains " + c + " at position " + 1);
        }
        c = (char) msgBody[1 + 1];
        if (c < '\u0026' || c > '\u0061') {
            throw new Exception("Raw packet contains " + c + " at position " + 2);
        }
        c = (char) msgBody[1 + 2];
        if (c < '\u001c' || c > '\u007f') {
            throw new Exception("Raw packet contains " + c + " at position " + 3);
        }
        c = (char) msgBody[1 + 3];
        if (c < '\u001c' || c > '\u007f') {
            throw new Exception("Raw packet contains " + c + " at position " + 4);
        }
        c = (char) msgBody[1 + 4];
        if (c < '\u001c' || c > '\u007d') {
            throw new Exception("Raw packet contains " + c + " at position " + 5);
        }
        c = (char) msgBody[1 + 5];
        if (c < '\u001c' || c > '\u007f') {
            throw new Exception("Raw packet contains " + c + " at position " + 6);
        }
        c = (char) msgBody[1 + 6];
        if ((c < '\u0021' || c > '\u007b') && (c != '\u007d')) {
            throw new Exception("Raw packet contains " + c + " at position " + 7);
        }
        if (!validSymTableUncompressed((char) msgBody[1 + 7])) {
            throw new Exception("Raw packet contains " + c + " at position " + 8);
        }
        char[] destcall2 = new char[6];
        for (int i = 0; i < 6; ++i) {
            c = destcall[i];
            if ('A' <= c && c <= 'J') {
                destcall2[i] = (char) (c - ('A' - '0')); // cast silences warning
            } else if ('P' <= c && c <= 'Y') {
                destcall2[i] = (char) (c - ('P' - '0')); // cast silences warning
            } else if ('K' == c || 'L' == c || 'Z' == c) {
                destcall2[i] = '_';
            } else
                destcall2[i] = c;
        }
        int posAmbiguity = 0;
        if (destcall2[5] == '_') {
            destcall2[5] = '5';
            posAmbiguity = 4;
        }
        if (destcall2[4] == '_') {
            destcall2[4] = '5';
            posAmbiguity = 3;
        }
        if (destcall2[3] == '_') {
            destcall2[3] = '5';
            posAmbiguity = 2;
        }
        if (destcall2[2] == '_') {
            destcall2[2] = '3';
            posAmbiguity = 1;
        }
        if (destcall2[1] == '_' || destcall2[0] == '_') {
            throw new Exception("bad pos-ambiguity on destcall");
        }

        double lat = 0.0F;
        try {
            lat = parseDegMin(destcall2, 0, 2, 9, false);
        } catch (Exception e) {
            throw new Exception("Destination Call invalid for MicE:  " + new String(destcall2));
        }
        // Check north/south direction, and correct the latitude sign if
        // necessary
        if (destinationCall.charAt(3) <= 'L') {
            lat = 0.0F - lat;
        }

        // Now parsing longitude
        int longDeg = (char) msgBody[1 + 0] - 28;
        if ((char) destcall[4] >= 'P')
            longDeg += 100;
        if (longDeg >= 180 && longDeg <= 189)
            longDeg -= 80;
        else if (longDeg >= 190 && longDeg <= 199)
            longDeg -= 190;
        int longMin = (char) msgBody[1 + 1] - 28;
        if (longMin >= 60)
            longMin -= 60;
        int longMinFract = (char) msgBody[1 + 2] - 28;

        float lng = 0.0F;

        switch (posAmbiguity) { // degree of positional ambiguity
            case 0:
                lng = ((float) longDeg + ((float) longMin) / 60.0F + ((float) longMinFract / 6000.0F));
                break;
            case 1:
                lng = ((float) longDeg + ((float) longMin) / 60.0F + ((float) (longMinFract - longMinFract % 10 + 5) / 6000.0F));
                break;
            case 2:
                lng = ((float) longDeg + ((float) longMin) / 60.0F);
                break;
            case 3:
                lng = ((float) longDeg + ((float) (longMin - longMin % 10 + 5)) / 60.0F);
                break;
            case 4:
                lng = ((float) longDeg + 0.5F);
                break;
            default:
                throw new Exception("Unable to extract longitude from MicE");
        }
        if ((char) destcall[1 + 4] >= 'P') { // Longitude east/west sign
            lng = 0.0F - lng; // east positive, west negative
        }
        return new Position((double) lat, (double) lng, posAmbiguity);//, (char) msgBody[1 + 7], (char) msgBody[1 + 6]);
    }

    private static boolean validSymTableUncompressed(char c) {
        if (c == '/' || c == '\\')
            return true;
        if ('A' <= c && c <= 'Z')
            return true;
        if ('0' <= c && c <= '9')
            return true;
        return false;
    }

    private static final int TimeStampLength = 7;
}
