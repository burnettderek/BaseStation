package com.decibel.civilianc2.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by dburnett on 3/30/2018.
 */

public class ViewBevel extends View {
    public ViewBevel(Context context) {
        super(context);
        init(context);
    }

    public ViewBevel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewBevel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        Object tag = getTag();
        if(tag instanceof String) {
            String[] sectorColor = ((String) tag).split(":");
            sector = sectorColor[0];
            if(sectorColor.length > 1){
                color = Color.parseColor(sectorColor[1]);
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        getDrawingRect(drawingRect);
        Path path = null;

        paint.setColor(color);
        paint.setAntiAlias(true);


        if(sector != null && !sector.isEmpty()){
            switch (sector){
                case "TopLeft":
                    path = getTopLeft(drawingRect);
                    break;
                case "TopRight":
                    path = getTopRight(drawingRect);
                    break;
                case "BottomLeft":
                    path = getBottomLeft(drawingRect);
                    break;
                case "BottomRight":
                    path = getBottomRight(drawingRect);
                    break;
            }
        }
        if(path != null)
            canvas.drawPath(path, paint);
    }

    private Path getTopLeft(Rect rect){
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(rect.width(), 0);
        path.lineTo(0, rect.height());
        return path;
    }

    private Path getTopRight(Rect rect){
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(rect.width(), 0);
        path.lineTo(rect.width(), rect.height());
        return path;
    }

    private Path getBottomLeft(Rect rect){
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(rect.width(), rect.height());
        path.lineTo(0, rect.height());
        return path;
    }

    private Path getBottomRight(Rect rect){
        Path path = new Path();
        path.moveTo(rect.width(), 0);
        path.lineTo(rect.width(), rect.height());
        path.lineTo(0, rect.height());
        return path;
    }

    private String sector;
    private int color;
    private Rect drawingRect = new Rect();
    private Paint paint = new Paint();
}
