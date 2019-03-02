package com.decibel.civilianc2.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import com.decibel.civilianc2.graphics.Ellipse;

/**
 * Created by dburnett on 1/19/2018.
 */

public class Guage extends View {

    public Guage(Context context) {
        super(context);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2f);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
    }

    public Guage(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2f);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
    }

    public Guage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2f);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas){
        paint.setStrokeWidth(2.0f);
        ellipse.draw(canvas, paint);
        canvas.drawCircle(150f, 150f, 100f, paint);
    }

    Ellipse ellipse = new Ellipse(180, 360, 10, 10, 100 - 10, new PointF(150f, 150f));
    Paint paint = new Paint();
}
