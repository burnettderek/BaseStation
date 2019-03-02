package com.decibel.civilianc2.model.managers;

import com.decibel.civilianc2.model.dataaccess.LocationReports;
import com.decibel.civilianc2.model.dataaccess.UserSettings;
import com.decibel.civilianc2.model.entities.Station;
import com.decibel.civilianc2.modems.fsk.Demodulator;
import com.decibel.civilianc2.modems.fsk.IDemodulatorListener;
import com.decibel.civilianc2.modems.fsk.Modulator;
import com.decibel.civilianc2.protocols.ax25.AX25Decoder;
import com.decibel.civilianc2.protocols.ax25.AX25Packet;
import com.decibel.civilianc2.protocols.ax25.aprs.APRSMessage;
import com.decibel.civilianc2.protocols.ax25.aprs.APRSPositionMessage;
import com.decibel.civilianc2.protocols.ax25.aprs.ItemDefinition;
import com.decibel.civilianc2.protocols.ax25.aprs.ObjectDefinition;
import com.decibel.civilianc2.protocols.ax25.aprs.PositionReport;
import com.decibel.civilianc2.protocols.ax25.aprs.StatusMessage;
import com.decibel.civilianc2.protocols.ax25.aprs.TextMessage;
import com.decibel.civilianc2.radios.ITransmitter;
import com.decibel.civilianc2.tools.Logger;

import java.util.ArrayList;
import java.util.Date;

import java.security.MessageDigest;
import java.util.List;

/**
 * Created by dburnett on 1/10/2018.
 * This class listens to incoming radio messages and then
 * implements the logic of how those messages effect the system.
 */

public class MessageInterpreter implements IDemodulatorListener{
    public MessageInterpreter(Demodulator demodulator, StationManager manager, PositionManager locations, MessageManager messageManager, AX25MessageManager ax25MessageManager, UserSettings userSettings, ITransmitter transmitter){
        demodulator.addListener(this);
        this.stationManager = manager;
        this.locationReports = locations;
        this.messageManager = messageManager;
        this.userSettings = userSettings;
        this.transmitter = transmitter;
        this.ax25MessageManager = ax25MessageManager;
    }

    @Override
    public void onAudioLevelChanged(int audioLevel) {

    }

    @Override
    public void onPacketReceived(byte[] bytes) {
        log.log(Logger.Level.DEBUG, "Decoding AX25 packet.");
        AX25Packet packet = decoder.Decode(bytes);
        if(packet != null){
            log.log(Logger.Level.DEBUG, "Packet decoded : " + packet);
            if(packet.Payload instanceof APRSMessage){
                APRSMessage message = (APRSMessage)packet.Payload;
                if(message instanceof PositionReport){
                    this.ax25MessageManager.addMessage(packet, true);
                    log.log(Logger.Level.INFO, "Packet was position report.");
                    PositionReport positionReport = (PositionReport)message;
                    this.onPositionReport(packet.Header.SourceAddress, positionReport);
                    for(IMessageListener listener : listeners){
                        listener.onPositionReport(packet, positionReport);
                    }
                } else if(message instanceof StatusMessage){
                    this.ax25MessageManager.addMessage(packet, true);
                    log.log(Logger.Level.INFO, "Packet was status message.");
                    StatusMessage statusMessage = (StatusMessage)message;
                    this.onStatusMessage(packet.Header.SourceAddress, statusMessage);
                    for(IMessageListener listener : listeners){
                        listener.onStatusMessage(packet, statusMessage);
                    }
                } else if(message instanceof ObjectDefinition){
                    this.ax25MessageManager.addMessage(packet, true);
                    log.log(Logger.Level.INFO, "Packet was object definition.");
                    ObjectDefinition objectDefinition = (ObjectDefinition)message;
                    this.onObjectDefinition(objectDefinition);
                    for(IMessageListener listener : listeners){
                        listener.onObjectDefinition(packet, objectDefinition);
                    }
                } else if(message instanceof ItemDefinition){
                    this.ax25MessageManager.addMessage(packet, true);
                    log.log(Logger.Level.INFO, "Packet was item definition.");
                    ItemDefinition itemDefinition = (ItemDefinition)message;
                    this.onItemDefinition(itemDefinition);
                    for(IMessageListener listener : listeners){
                        listener.onItemDefinition(packet, itemDefinition);
                    }
                }
                else if (message instanceof TextMessage) {
                    this.ax25MessageManager.addMessage(packet, true);
                    log.log(Logger.Level.INFO, "Packet was text message.");
                    TextMessage textMessage = (TextMessage)message;
                    this.onTextMessage(packet, textMessage);
                    for(IMessageListener listener : listeners){
                        listener.onTextMessage(packet, textMessage);
                    }
                } else {
                    log.log(Logger.Level.INFO, "Packet was an unsupported AX25 message.");
                    this.ax25MessageManager.addMessage(packet, false);
                }
            }
        }
    }

    private void onTextMessage(AX25Packet packet, TextMessage textMessage) {
        messageManager.addMessage(packet.Header.SourceAddress, packet.Header.DestinationAddress, textMessage.Message, textMessage.Acknowledge, textMessage.MessageId);
        String callSign = userSettings.getSetting(UserSettings.CallSign);
        if(textMessage.Acknowledge || callSign != null && !callSign.isEmpty() && callSign.equals(packet.Header.DestinationAddress)){
            ackMessage(callSign, packet.Header.SourceAddress, textMessage);
        }
    }

    private void ackMessage(String callSign, String sender, TextMessage textMessage){
        TextMessage ack = new TextMessage();
        ack.Message = "ack" + textMessage.MessageId;
        AX25Packet packet = new AX25Packet();
        packet.Header.SourceAddress = callSign;
        packet.Header.DestinationAddress = sender;
        Modulator modulator = new Modulator(transmitter);
        modulator.sendMessage(packet);
    }

    @Override
    public void onRunningChanged(boolean running) {

    }

    private void onItemDefinition(ItemDefinition itemDefinition) {
    }

    private void onStatusMessage(String callSign, StatusMessage statusMessage){
        stationManager.updateStatus(callSign, statusMessage.Status);
    }

    private void onObjectDefinition(ObjectDefinition objectDefinition) {
        Station station = new Station(objectDefinition.Name);
        station.setSymbol(objectDefinition.Symbol);
        if(stationManager.hasStation(station.getCallsign())){
            stationManager.updateStation(station);
        } else {
            stationManager.addStation(station);
        }

        if(objectDefinition.Position != null) {
            locationReports.addPositionReport(station, new com.decibel.civilianc2.model.entities.PositionReport(objectDefinition.Position, new Date()));
        }
    }

    private void onPositionReport(String callsign, PositionReport positionReport) {
        Station station = new Station(callsign);
        station.setSymbol(positionReport.Symbol);
        if(stationManager.hasStation(station.getCallsign())){
            stationManager.updateStation(station);
        } else {
            stationManager.addStation(station);
            if(positionReport.Position != null){
                locationReports.addPositionReport(station, new com.decibel.civilianc2.model.entities.PositionReport(positionReport.Position, new Date()));
            }
        }
    }

    public interface IMessageListener {
        void onPositionReport(AX25Packet packet, PositionReport positionReport);
        void onObjectDefinition(AX25Packet packet, ObjectDefinition objectDefinition);
        void onItemDefinition(AX25Packet packet, ItemDefinition definition);
        void onStatusMessage(AX25Packet packet, StatusMessage message);
        void onTextMessage(AX25Packet packet, TextMessage message);
    }

    public void addEventListener(IMessageListener listener){
        listeners.add(listener);
    }

    public void removeEventListener(IMessageListener listener){
        listeners.remove(listener);
    }

    private AX25Decoder decoder = new AX25Decoder();
    private StationManager stationManager;
    private PositionManager locationReports;
    private final MessageManager messageManager;
    private final UserSettings userSettings;
    private final ITransmitter transmitter;
    private final AX25MessageManager ax25MessageManager;
    private Logger<MessageInterpreter> log = new Logger<>(this);

    private List<IMessageListener> listeners = new ArrayList<>();
}
