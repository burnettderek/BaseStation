package com.decibel.civilianc2.graphics;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;

/**
 * Created by dburnett on 1/19/2018.
 */

public class Ellipse {
    public Ellipse(float startAngle, float endAngle, float ticSeparation, float ticLength, float radius, PointF center){
        this.startAngle = startAngle;
        this.endAngle = endAngle;
        this.ticSeparation = ticSeparation;
        this.ticLength = ticLength;
        this.radius = radius;
        this.center = center;
    }

    public void draw(Canvas canvas, Paint paint){
        float[] src = {0.0f, 0.0f, ticLength, 0.0f};
        for(float i = startAngle; i < endAngle; i+= ticSeparation){
            matrix.reset();
            matrix.postTranslate(radius, 0.0f);
            matrix.postRotate(i);
            matrix.postTranslate(center.x, center.y);
            float[] dest = new float[4];
            matrix.mapPoints(dest, src);
            canvas.drawLine(dest[0], dest[1], dest[2], dest[3], paint);
        }
    }

    private float startAngle;
    private float endAngle;
    private float radius;
    private PointF center;
    private float ticSeparation;
    private float ticLength;
    private Matrix matrix = new Matrix();
}
