package com.decibel.civilianc2.maprendering;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;

import com.decibel.civilianc2.graphics.Rectangle;
import com.decibel.civilianc2.model.entities.Position;
import com.decibel.civilianc2.model.entities.PositionReport;
import com.decibel.civilianc2.model.entities.Station;
import com.decibel.civilianc2.model.managers.PositionManager;
import com.decibel.civilianc2.model.managers.StationManager;
import com.decibel.civilianc2.tools.Logger;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.HashMap;
import java.util.List;

/**
 * Created by dburnett on 3/20/2018.
 */

public class OffscreenIndicatorOverlay extends Overlay implements StationManager.IEventListener {
    public OffscreenIndicatorOverlay(Context context, StationManager manager, PositionManager positionManager){
        this.context = context;
        this.positionManager = positionManager;
        this.stationManager = manager;
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
        osmv.getScreenRect(screenRect);
        center.x = screenRect.exactCenterX();
        center.y = screenRect.exactCenterY();
        screenBounds.set(screenRect);
        screenBounds.inset(Margin, Margin);
        for(HashMap.Entry<String, APRSSymbolRenderer> entry : renderers.entrySet()){
            //entry.getValue().onDraw(c, osmv);
            if(!isOnScreen(entry.getValue().getStation(), this.positionManager, osmv, currentPoint)){
                screenPos.set(currentPoint.x, currentPoint.y);
                indicatorPos = screenBounds.getIntersection(center, screenPos);
                entry.getValue().drawIndicator(c, indicatorPos);
            }
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

    public void onDestroy(){
        stationManager.removeEventListener(this);
    }

    public boolean isOnScreen(Station station, PositionManager manager, MapView mapView, Point out){
        List<PositionReport> reports = manager.getPositionReports(station);
        if(reports.size() == 0) return true; // no position so we don't care.
        Position position = reports.get(0).getPosition();
        geoPoint.setLatitude(position.getLongitude());
        geoPoint.setLongitude(position.getLatitude());
        mapView.getProjection().toPixels(geoPoint, out);
        return screenRect.contains(out.x, out.y);
    }

    private Paint paint = new Paint();
    private HashMap<String, APRSSymbolRenderer> renderers = new HashMap<>();
    private Context context;
    private PositionManager positionManager;
    private APRSImageLibrary symbols;
    private Handler handler = new Handler();
    private StationManager stationManager;
    private Logger<OffscreenIndicatorOverlay> log = new Logger<>(this);
    private Point currentPoint = new Point();
    private Rect screenRect = new Rect();
    private PointF center = new PointF();
    private Rectangle screenBounds = new Rectangle();
    private PointF screenPos = new PointF();
    private PointF indicatorPos = new PointF();
    private GeoPoint geoPoint = new GeoPoint(0, 0);

    private static final float Margin = 45f;
}
