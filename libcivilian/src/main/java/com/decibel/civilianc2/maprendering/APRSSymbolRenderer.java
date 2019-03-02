package com.decibel.civilianc2.maprendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.decibel.civilianc2.model.entities.PositionReport;
import com.decibel.civilianc2.model.entities.Station;
import com.decibel.civilianc2.model.entities.Symbol;
import com.decibel.civilianc2.model.managers.Model;
import com.decibel.civilianc2.model.managers.PositionManager;
import com.decibel.civilianc2.tools.Logger;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.Date;
import java.util.List;

/**
 * Created by dburnett on 12/29/2017.
 */

public class APRSSymbolRenderer {
    public APRSSymbolRenderer(Context context, Station station, PositionManager positionManager, APRSImageLibrary imageLibrary){
        if(typeface == null){
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/whitrabt.ttf");
        }
        this.positionManager = positionManager;
        this.station = station;
        if(station.getSymbol() != null) {
            log.log(Logger.Level.INFO, station.getCallsign() + " symbol is " + station.getSymbol().getSymbolTable() + ":" + station.getSymbol().getSymbolIndex() + ".");
            this.symbolBitmap = imageLibrary.getImage(station.getSymbol().getSymbolTable(), station.getSymbol().getSymbolIndex(), typeface);
        }
        else {
            log.log(Logger.Level.INFO, station.getCallsign() + " does not currently have a symbol associated with them.");
            this.symbolBitmap = imageLibrary.getImage(new String() + Symbol.PRIMARY_SYMBOL_TABLE, 1, typeface);
        }
        this.symbolRect = new Rect(0, 0, symbolBitmap.getWidth(), symbolBitmap.getHeight());
        this.positionReports = positionManager.getPositionReports(station);
    }

    public void onDraw(Canvas canvas, MapView osmv){

        if(positionReports.size() > 0){
            PositionReport positionReport = positionReports.get(0);
            GeoPoint startPoint = new GeoPoint(positionReport.getPosition().getLongitude(), positionReport.getPosition().getLatitude());
            osmv.getProjection().toPixels(startPoint, screen);
            osmv.getScreenRect(screenRect);
            symbolScreenRect.set(screen.x - symbolRect.centerX(), screen.y - symbolRect.centerY(), screen.x + symbolRect.centerX(), screen.y + symbolRect.centerY());
            if(!screenRect.contains(screen.x, screen.y)) return; //we're offscreen. skip everything about to happen.


            Date lastUpdated = Model.getInstance().getStatistics().get(station.getCallsign()).getLastHeard();
            if(lastUpdated != null) {
                long delta = new Date().getTime() - lastUpdated.getTime();
                double compare = (double) delta / (double) FadeTimeInMs;
                colorScale = 1.0 - compare;
                if (colorScale < 0) colorScale = 0;
            }


            matrix.setSaturation((float)colorScale);
            paint.setColorFilter(new ColorMatrixColorFilter(matrix));
            paint.setAlpha(128 + (int)(128 * colorScale));
            canvas.drawBitmap(symbolBitmap, symbolRect, symbolScreenRect, paint);
            paint.setColorFilter(null);

            paint.setAntiAlias(true);
            paint.setTextSize(20); //28
            paint.setTypeface(typeface);
            Rect textBounds = new Rect();
            paint.getTextBounds(station.getCallsign(), 0, station.getCallsign().length(), textBounds);
            textBounds.offset(screen.x - textBounds.centerX(), screen.y + symbolRect.height() -2); //24
            paint.setColor(Color.argb(128, 0, 0, 0));
            canvas.drawRoundRect(new RectF(textBounds.left - 2, textBounds.top - 2, textBounds.right + 2, textBounds.bottom + 2), 4.0f, 4.0f, paint);
            paint.setColor(Color.WHITE);

            canvas.drawText(station.getCallsign(), textBounds.left, textBounds.bottom, paint);

            //canvas.drawText(station.getCallsign(), screen.x - textBounds.centerX(), screen.y + symbolRect.height(), paint);
            //canvas.drawText(station.getCallsign(), screen.x - textBounds.centerX(), screen.y + symbolRect.height(), paint);
        }
    }

    public void drawIndicator(Canvas canvas, PointF position){
        //canvas.drawBitmap(symbolBitmap, position.x, position.y - symbolRect.height() / 2, paint);


        paint.setAntiAlias(true);
        paint.setTextSize(28); //28
        paint.setTypeface(typeface);
        Rect textBounds = new Rect();
        paint.getTextBounds(station.getCallsign(), 0, station.getCallsign().length(), textBounds);
        textBounds.offset((int)position.x - textBounds.centerX(), (int)position.y - textBounds.centerY()); //24
        paint.setColor(Color.argb(128, 0, 0, 0));
        canvas.drawRoundRect(new RectF(textBounds.left - 2, textBounds.top - 2, textBounds.right + 24, textBounds.bottom + 2), 4.0f, 4.0f, paint);
        canvas.drawBitmap(symbolBitmap, new Rect(0, 0, symbolRect.width(), symbolRect.height()), new Rect(textBounds.right + 2, textBounds.top, textBounds.right + 22, textBounds.bottom), paint);
        paint.setColor(Color.WHITE);

        canvas.drawText(station.getCallsign(), textBounds.left, textBounds.bottom, paint);
    }

    public void updateStation(Station station){

        this.station = station;
        this.positionReports = positionManager.getPositionReports(station);
    }


    public Station getStation(){
        return this.station;
    }

    public Rect getSymbolScreenRect(){
        return this.symbolScreenRect;
    }


    private Station station;
    private Point screen = new Point();
    private Bitmap symbolBitmap;
    private Paint paint = new Paint();
    private List<PositionReport> positionReports;
    private Rect symbolRect;
    private static Typeface typeface;
    private PositionManager positionManager;
    private ColorMatrix matrix = new ColorMatrix();
    private static long FadeTimeInMs = 1000 * 60 * 30; //last multiple is minutes (30 minutes)
    private double colorScale = 0.0;
    private Rect screenRect = new Rect();
    private Rect symbolScreenRect = new Rect();
    private Logger<APRSSymbolRenderer> log = new Logger<>(this);
}
