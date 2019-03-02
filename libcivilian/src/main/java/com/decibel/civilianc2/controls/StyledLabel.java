package com.decibel.civilianc2.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by dburnett on 2/16/2018.
 */

public class StyledLabel extends TextView{

    public enum Style {
        PinPoint,
        Dogear
    }

    public StyledLabel(Context context) {
        super(context);
        initialize(context);
    }

    public StyledLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public StyledLabel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public StyledLabel(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    private void initialize(Context context) {
        try {
            if (typeface == null) {
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Exo2-Regular.ttf");
            }
        }catch (Exception e){
            typeface = Typeface.DEFAULT;
        }
        Object tag = getTag();
        if(tag != null){
            String textStyle = (String)tag;
            switch(textStyle){
                case "DogEar":
                    mode = Style.Dogear;
                    break;
                case "PinPoint":
                    mode = Style.PinPoint;
                    break;
            }
        }
        setPadding(5, 0, 15, 0);
        setTextSize(8f);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        this.getDrawingRect(drawingRect);


        String text = getText().toString();
        paint.setColor(getCurrentTextColor());

        canvas.drawRect(drawingRect.left, drawingRect.top, drawingRect.right - 10f, drawingRect.bottom, paint);
        switch (mode){
            case PinPoint:
                drawPinpoint(canvas);
                break;
            case Dogear:
                drawDogEar(canvas);
        }


        paint.setTextSize(getTextSize());
        paint.setTypeface(typeface);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        canvas.drawText(text, getPaddingLeft(), drawingRect.centerY() - textBounds.centerY(), paint);
    }

    private void drawPinpoint(Canvas canvas) {
        Path path = new Path();
        path.moveTo(drawingRect.right - 10f, drawingRect.top);
        path.lineTo(drawingRect.right - 10f + drawingRect.centerY(), drawingRect.centerY());
        path.lineTo(drawingRect.right - 10f, drawingRect.bottom);
        canvas.drawPath(path, paint);
    }

    private void drawDogEar(Canvas canvas) {
        Path path = new Path();
        path.moveTo(drawingRect.right - 10f, drawingRect.top);
        path.lineTo(drawingRect.right + 7f, drawingRect.top);
        path.lineTo(drawingRect.right - 10f, drawingRect.bottom);
        canvas.drawPath(path, paint);
    }

    private Rect drawingRect = new Rect();
    private Rect textBounds = new Rect();
    private Paint paint = new Paint();
    private static Typeface typeface;
    private Style mode = Style.PinPoint;
}
