package com.decibel.civilianc2.maprendering;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;

import com.decibel.civilianc2.model.entities.Position;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dburnett on 1/17/2018.
 */

public class PingOverlay extends Overlay {
    public PingOverlay(MapView mv){
        this.mapView = mv;
        this.timer = new Timer("Ping Animation Timer");
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                onUpdate();
            }
        }, 0, 30);

        paint.setAntiAlias(true);
        paint.setStrokeWidth(2.0f);
        paint.setStyle(Paint.Style.STROKE);
    }
    @Override
    public void draw(Canvas c, MapView osmv, boolean shadow) {
        Point point = new Point();

        for(Ping ping : pings) {
            paint.setColor(Color.argb(((int)(255f * (1.0f - (ping.RadiusInPixels/MaxRadiusInPixels)))), 0, 128, 255));
            GeoPoint startPoint = new GeoPoint(ping.Position.getLongitude(), ping.Position.getLatitude());
            osmv.getProjection().toPixels(startPoint, point);
            c.drawCircle(point.x, point.y, ping.RadiusInPixels, paint);
        }
    }

    public void addPing(Position p){
        Ping ping = new Ping();
        ping.Position = p;
        ping.RadiusInPixels = 0f;
        pings.add(ping);
    }

    private class Ping{
        Position Position;
        float RadiusInPixels;
    }

    protected void onUpdate(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                List<Ping> deletes = new ArrayList<>();
                if(pings.size() > 0){
                    for(Ping ping : pings){
                        ping.RadiusInPixels += 0.5f;
                        if(ping.RadiusInPixels > MaxRadiusInPixels){
                            deletes.add(ping);
                        }
                    }
                    for(Ping ping : deletes){
                        pings.remove(ping);
                    }
                    mapView.invalidate();
                }
            }
        });
    }

    private List<Ping> pings = new ArrayList<>();
    private MapView mapView;
    private Timer timer;
    private Handler handler = new Handler();
    private final static float MaxRadiusInPixels = 500.0f;
    private Paint paint = new Paint();
}
