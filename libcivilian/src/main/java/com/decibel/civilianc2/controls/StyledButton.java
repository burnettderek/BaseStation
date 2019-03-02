package com.decibel.civilianc2.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * Created by dburnett on 2/16/2018.
 */

public class StyledButton extends Button {

    public StyledButton(Context context) {
        super(context);
        initialize(context);

    }

    public StyledButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public StyledButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }


    private void initialize(Context context) {
        if(typeface == null){
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Exo2-Regular.ttf");
        }
        this.setBackground(null);
        this.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    setPressed(true);
                    return true;
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    setPressed(false);
                    performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        this.getDrawingRect(drawingRect);

        String text = getText().toString();
        paint.setColor(getCurrentTextColor());
        if(!pressed) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth((float) borderThickness);
        } else
            paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(false);
        float offset = borderThickness / 2.0f;
        canvas.drawRect(drawingRect.left + offset, drawingRect.top + offset, drawingRect.right - offset, drawingRect.bottom - offset, paint);
        paint.setTextSize(getTextSize());
        paint.setTypeface(typeface);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        if(pressed)paint.setColor(Color.BLACK);
        canvas.drawText(text, drawingRect.centerX() - textBounds.centerX(), drawingRect.centerY() - textBounds.centerY(), paint);
    }

    public void setPressed(boolean pressed){
        this.pressed = pressed;
        this.invalidate();
    }

    public int getBorderThickness() {
        return borderThickness;
    }

    public void setBorderThickness(int borderThickness) {
        this.borderThickness = borderThickness;
    }


    private int borderThickness = 1;
    private Rect drawingRect = new Rect();
    private Rect textBounds = new Rect();
    private Paint paint = new Paint();
    private static Typeface typeface;
    private boolean pressed = false;

}
