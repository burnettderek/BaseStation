package com.decibel.civilianc2.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by dburnett on 2/21/2018.
 */

public class ViewBorder extends FrameLayout {
    public ViewBorder(Context context) {
        super(context);
        initialize(context);
    }

    public ViewBorder(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public ViewBorder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public ViewBorder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    private void initialize(Context context){
        this.setWillNotDraw(false);
        Object tag = getTag();
        if(tag != null) { //they specified a color
            String colorText = (String)tag;
            borderColor = Color.parseColor(colorText);
        }
        int padding = getPaddingStart();
        if(padding > 1){
            borderWidth = padding;
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        this.getDrawingRect(drawingRect);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        paint.setAntiAlias(false);
        paint.setColor(borderColor);
        drawingRect.set(drawingRect.left, drawingRect.top, drawingRect.right - 1, drawingRect.bottom - 1);
        canvas.drawRect(drawingRect, paint);
    }

    private Paint paint = new Paint();
    private Rect drawingRect = new Rect();
    private int borderColor = Color.WHITE;
    private int borderWidth = 2;
}
