package com.decibel.civilianc2.activities;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.decibel.civilianc2.controls.AudioLevel;
import com.decibel.civilianc2.maprendering.APRSOverlay;
import com.decibel.civilianc2.maprendering.APRSSymbolRenderer;
import com.decibel.civilianc2.maprendering.OffscreenIndicatorOverlay;
import com.decibel.civilianc2.maprendering.PingOverlay;
import com.decibel.civilianc2.model.dataaccess.UserSettings;
import com.decibel.civilianc2.model.entities.Position;
import com.decibel.civilianc2.model.entities.Station;
import com.decibel.civilianc2.model.entities.Symbol;
import com.decibel.civilianc2.model.managers.MessageInterpreter;
import com.decibel.civilianc2.model.managers.Model;
import com.decibel.civilianc2.model.managers.StationManager;
import com.decibel.civilianc2.modems.fsk.Demodulator;
import com.decibel.civilianc2.modems.fsk.IDemodulatorListener;
import com.decibel.civilianc2.modems.fsk.Modulator;
import com.decibel.civilianc2.protocols.ax25.AX25Packet;
import com.decibel.civilianc2.protocols.ax25.aprs.APRSMessage;
import com.decibel.civilianc2.protocols.ax25.aprs.ItemDefinition;
import com.decibel.civilianc2.protocols.ax25.aprs.ObjectDefinition;
import com.decibel.civilianc2.protocols.ax25.aprs.PositionReport;
import com.decibel.civilianc2.protocols.ax25.aprs.StatusMessage;
import com.decibel.civilianc2.protocols.ax25.aprs.TextMessage;
import com.decibel.civilianc2.R;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.Date;
import java.util.List;

public class MapActivity extends Activity implements StationManager.IEventListener, MapListener, IDemodulatorListener, MessageInterpreter.IMessageListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Configuration.getInstance().load(this.getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()));
        map = (MapView) findViewById(R.id.map);
        powerToggle = (ToggleButton)findViewById(R.id.btnPowerToggle);
        audioLevel = (AudioLevel) findViewById(R.id.audioDisplay);
        txtMessageCallsign = (TextView)findViewById(R.id.txtMessageCallsign);
        txtTimeStamp = (TextView)findViewById(R.id.txtTimeStamp);
        txtMessage = (TextView)findViewById(R.id.txtMessage);
        txtMessageCallsign.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/whitrabt.ttf"));
        txtMessageCallsign.setText("");
        txtTimeStamp.setText("");
        txtMessage.setText("");
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        AX25Packet lastMessage = Model.getInstance().getLastMessage();
        if(lastMessage != null){
            if(lastMessage.Payload instanceof TextMessage){
                TextMessage textMessage = (TextMessage)lastMessage.Payload;
                onTextMessage(lastMessage, textMessage);
            } else if (lastMessage.Payload instanceof PositionReport){
                onPositionReport(lastMessage, (PositionReport)lastMessage.Payload);
            } else if (lastMessage.Payload instanceof ObjectDefinition){
                onObjectDefinition(lastMessage, (ObjectDefinition)lastMessage.Payload);
            } else if (lastMessage.Payload instanceof ItemDefinition){
                onItemDefinition(lastMessage, (ItemDefinition)lastMessage.Payload);
            } else if (lastMessage.Payload instanceof StatusMessage){
                onStatusMessage(lastMessage, (StatusMessage)lastMessage.Payload);
            }
        }
        audioLevel.setAudioLevel(0);

        map.setUseDataConnection(true);
        map.setTileSource(TileSourceFactory.USGS_TOPO);
        map.setMapListener(new DelayedMapListener(this, 1000));

        powerToggle.setChecked(Demodulator.getInstance().isRunning());
        powerToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                    Demodulator.getInstance().startListeningToMic();
                else
                    Demodulator.getInstance().stopListeningToMic();
            }
        });
        Demodulator.getInstance().addListener(this);
        Model.getInstance().getMessageInterpreter().addEventListener(this);

        final IMapController mapController = map.getController();

        String center = Model.getInstance().getUserSettings().getSetting("CurrentMap.Center");
        zoom = Model.getInstance().getUserSettings().getInt("CurrentMap.Zoom", 13);

        if(center == null){
            GeoPoint startPoint = new GeoPoint(38.7874, -90.6298);
            mapController.setCenter(startPoint);

        } else {
            GeoPoint point = GeoPoint.fromDoubleString(center, ',');
            mapController.setCenter(point);
        }
        mapController.setZoom(zoom);

        map.setMultiTouchControls(true);


        overlay = new APRSOverlay(this, Model.getInstance().getStationManager(), Model.getInstance().getPositionManager());
        map.getOverlayManager().add(overlay);

        pingOverlay = new PingOverlay(map);
        map.getOverlayManager().add(pingOverlay);

        offscreenIndicatorOverlay = new OffscreenIndicatorOverlay(this, Model.getInstance().getStationManager(), Model.getInstance().getPositionManager());
        map.getOverlayManager().add(offscreenIndicatorOverlay);

        Model.getInstance().getStationManager().addEventListener(this);


        map.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_SCROLL:
                        float val = motionEvent.getAxisValue(MotionEvent.AXIS_VSCROLL);
                        zoom += (int)val;
                        if(zoom < 1)zoom = 1;
                        if(zoom > 15)zoom = 15;
                        mapController.setZoom(zoom);


                    case MotionEvent.ACTION_BUTTON_RELEASE:
                        onMapClick(new Point((int)motionEvent.getX(), (int)motionEvent.getY()));
                }
                return false;
            }
        });

        final CheckBox beacon = findViewById(R.id.beaconCheck);
        final boolean isChecked = false;
        beacon.setChecked(isChecked);

        beacon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    startBeacon();
                else
                    stopBeacon();
            }
        });

        /*ImageView sendPositionButton = findViewById(R.id.ReportPositionButton);
        sendPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportPosition();
            }
        });*/
    }

    private void stopBeacon() {
        /*if(Model.getInstance().getPositionBeacon() != null){
            Model.getInstance().getPositionBeacon().stop();
        }*/
    }

    private void startBeacon(){
        Station station = new Station("KR0GUE");
        Symbol symbol = new Symbol(Symbol.PRIMARY_SYMBOL_TABLE, Symbol.getSymbolIndex('-'));
        station.setSymbol(symbol);

        AX25Packet packet = new AX25Packet();
        packet.Header.SourceAddress = station.getCallsign();
        packet.Header.DestinationAddress = APRSMessage.GenericDigipeterAddress;
        com.decibel.civilianc2.protocols.ax25.aprs.PositionReport positionReport = new com.decibel.civilianc2.protocols.ax25.aprs.PositionReport();
        positionReport.Position = new Position(39.800454, -90.750227);
        positionReport.Symbol = symbol;
        packet.Payload = positionReport;
        byte[] bytes = Model.getInstance().getModulator().sendMessage(packet);
        Model.getInstance().getMessageInterpreter().onPacketReceived(bytes);
    }

    private void onMapClick(Point screenPoint){
        List<APRSSymbolRenderer> renderers = overlay.getScreenSymbols();
        for(APRSSymbolRenderer renderer : renderers){
            if(renderer.getSymbolScreenRect().contains(screenPoint.x, screenPoint.y)){
                onSymbolClicked(renderer);
                return;
            }
        }
    }

    private void onSymbolClicked(APRSSymbolRenderer renderer){
        String callSign = renderer.getStation().getCallsign();
        //Intent intent =  new Intent(this, SendMessage.class);
        //intent.putExtra(SendMessage.RecipientCallSignKey, callSign);
        //startActivity(intent);
    }


    @Override
    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Model.getInstance().getStationManager().removeEventListener(this);
        Model.getInstance().getMessageInterpreter().removeEventListener(this);
        Demodulator.getInstance().removeListener(this);
        overlay.onDestroy();
    }



    @Override
    public void onAdded(String callsign, final Station station) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                map.invalidate();
                List<com.decibel.civilianc2.model.entities.PositionReport> reports = Model.getInstance().getPositionManager().getPositionReports(station);
                if(reports.size() > 0){
                    pingOverlay.addPing(reports.get(0).getPosition());
                }
            }
        });
    }

    @Override
    public void onUpdated(String callsign, final Station station) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                map.invalidate();
                List<com.decibel.civilianc2.model.entities.PositionReport> reports = Model.getInstance().getPositionManager().getPositionReports(station);
                if(reports.size() > 0){
                    pingOverlay.addPing(reports.get(0).getPosition());
                }
            }
        });
    }

    @Override
    public void onRemoved(String callsign) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                map.invalidate();
            }
        });
    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        IGeoPoint point = map.getProjection().getBoundingBox().getCenter();
        Model.getInstance().getUserSettings().setSetting("CurrentMap.Center", point.toString());

        int zoom = map.getZoomLevel();
        Model.getInstance().getUserSettings().setSetting("CurrentMap.Zoom", Integer.toString(zoom));
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        return false;
    }

    @Override
    public void onAudioLevelChanged(final int level) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                audioLevel.setAudioLevel(level);
            }
        });
    }

    @Override
    public void onPacketReceived(byte[] bytes) {

    }

    @Override
    public void onRunningChanged(final boolean running){
        if(!running){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    audioLevel.setAudioLevel(0);
                }
            });
        }
    }

    @Override
    public void onPositionReport(final AX25Packet packet, final PositionReport positionReport) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(positionReport.Comment != null && positionReport.Comment.length() > 0){
                    txtMessage.setTextColor(Color.CYAN);
                    txtMessage.setText(positionReport.Comment);
                } else {
                    txtMessage.setTextColor(Color.YELLOW);
                    txtMessage.setText("<Reported Position>");
                }
                txtMessageCallsign.setText(packet.Header.SourceAddress);
                txtTimeStamp.setText(df.format("hh:mm a", new Date()));
            }
        });
    }

    @Override
    public void onObjectDefinition(final AX25Packet packet, final ObjectDefinition objectDefinition) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(objectDefinition.Comment != null && objectDefinition.Comment.length() > 0){
                    txtMessage.setTextColor(Color.CYAN);
                    txtMessage.setText(objectDefinition.Name + ":" + objectDefinition.Comment);
                } else {
                    txtMessage.setTextColor(Color.YELLOW);
                    txtMessage.setText(objectDefinition.Name + ":" + "<Reported Position>");
                }
                txtMessageCallsign.setText(packet.Header.SourceAddress);
                txtTimeStamp.setText(df.format("hh:mm a", new Date()));
            }
        });
    }

    @Override
    public void onItemDefinition(AX25Packet packet, ItemDefinition definition) {

    }

    @Override
    public void onStatusMessage(AX25Packet packet, final StatusMessage message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                txtMessage.setTextColor(Color.CYAN);
                txtMessage.setText("Status: " + message.Status);
            }
        });
    }

    @Override
    public void onTextMessage(final AX25Packet packet, final TextMessage message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                txtMessage.setTextColor(Color.CYAN);
                txtMessage.setText("Message: " + message.Message);
                txtMessageCallsign.setText(packet.Header.SourceAddress);
                txtTimeStamp.setText(df.format("hh:mm a", new Date()));
            }
        });
    }

    private void reportPosition() {
        String callsign = Model.getInstance().getUserSettings().getSetting(UserSettings.CallSign);
        if(callsign == null || callsign.isEmpty()){
            Toast.makeText(this, "You must set your callsign before you can broadcast your position.", Toast.LENGTH_LONG).show();
            return;
        }
        Symbol symbol = new Symbol(Symbol.PRIMARY_SYMBOL_TABLE, Symbol.getSymbolIndex('-'));

        Position position =  Model.getInstance().getUserSettings().getPosition(UserSettings.Location);

        if(position == null){
            Toast.makeText(this, "You must set your position before you can broadcast your position.", Toast.LENGTH_LONG).show();
            return;
        }

        String comment = Model.getInstance().getUserSettings().getSetting(UserSettings.APRSCommentField);
        if(comment == null)comment = new String();
        //modulator.sendPositionReport(callsign, symbol, position, comment, RadioDriverFactory.getInstance().getTransceiver(RadioDriverFactory.DRA818V_DRIVER_KEY));
        Station station = new Station(callsign);
        station.setSymbol(symbol);
        if(Model.getInstance().getStationManager().hasStation(callsign))
            Model.getInstance().getPositionManager().addPositionReport(station, new com.decibel.civilianc2.model.entities.PositionReport(position, new Date()));
        else {
            Model.getInstance().getStationManager().addStation(station);
            Model.getInstance().getPositionManager().addPositionReport(station, new com.decibel.civilianc2.model.entities.PositionReport(position, new Date()));
        }
    }

    private MapView map;
    private APRSOverlay overlay;
    private PingOverlay pingOverlay;
    private OffscreenIndicatorOverlay offscreenIndicatorOverlay;
    private Handler handler = new Handler();
    private ToggleButton powerToggle;
    private AudioLevel audioLevel;
    private int zoom = 13;

    private TextView txtMessageCallsign;
    private TextView txtTimeStamp;
    private TextView txtMessage;
    android.text.format.DateFormat df = new android.text.format.DateFormat();
}
