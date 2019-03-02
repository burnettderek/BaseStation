package com.decibel.civilianc2.maprendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;

import com.decibel.civilianc2.model.entities.Station;
import com.decibel.civilianc2.model.managers.PositionManager;
import com.decibel.civilianc2.model.managers.StationManager;
import com.decibel.civilianc2.tools.Logger;
import com.example.libcivilian.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dburnett on 12/29/2017.
 */

public class APRSOverlay extends Overlay implements StationManager.IEventListener {
    public APRSOverlay(Context context, StationManager manager, PositionManager positionManager){
        this.context = context;
        this.positionManager = positionManager;
        this.stationManager = manager;
        testImage = BitmapFactory.decodeResource(context.getApplicationContext().getResources(), R.drawable.direction_arrow);
        paint.setAntiAlias(true);
        this.symbols = new APRSImageLibrary(context);
        List<Station> stations = manager.getAll();
        for(Station station : stations){
            APRSSymbolRenderer renderer = new APRSSymbolRenderer(context, station, positionManager, this.symbols);
            renderers.put(station.getCallsign(), renderer);
        }
        manager.addEventListener(this);
    }


    @Override
    public void draw(Canvas c, MapView osmv, boolean shadow) {
        for(HashMap.Entry<String, APRSSymbolRenderer> entry : renderers.entrySet()){
            entry.getValue().onDraw(c, osmv);
        }
    }

    @Override
    public void onAdded(final String s, final Station station) {
        log.log(Logger.Level.DEBUG, "Adding new station '" + station.getCallsign() + "' to the map.");
        handler.post(new Runnable() {
            @Override
            public void run() {
                renderers.put(s, new APRSSymbolRenderer(context, station, positionManager, symbols));
            }
        });

    }

    @Override
    public void onUpdated(final String s, final Station station) {
        log.log(Logger.Level.DEBUG, "Updating station '" + station.getCallsign() + "' on map.");
        handler.post(new Runnable() {
            @Override
            public void run() {
                renderers.get(s).updateStation(station);
            }
        });

    }

    @Override
    public void onRemoved(final String s) {
        log.log(Logger.Level.DEBUG, "Removing station '" + s + "' from map.");
        handler.post(new Runnable() {
            @Override
            public void run() {
                renderers.remove(s);
            }
        });

    }

    public List<APRSSymbolRenderer> getScreenSymbols(){
        return new ArrayList<>(renderers.values());
    }

    public void onDestroy(){
        stationManager.removeEventListener(this);
    }

    private Bitmap testImage;
    private Paint paint = new Paint();
    private HashMap<String, APRSSymbolRenderer> renderers = new HashMap<>();
    private Context context;
    private PositionManager positionManager;
    private APRSImageLibrary symbols;
    private Handler handler = new Handler();
    private StationManager stationManager;
    private Logger<APRSOverlay> log = new Logger<>(this);
}
