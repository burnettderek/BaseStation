package com.decibel.civilianc2.controls;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

/**
 * Created by dburnett on 2/20/2018.
 */

public class StyledSeekBar extends SeekBar {
    public StyledSeekBar(Context context) {
        super(context);
        initialize(context);
    }

    public StyledSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public StyledSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    public StyledSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context);
    }

    private void initialize(Context context){
        if(typeface == null){
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Exo2-Regular.ttf");
        }
        setProgressDrawable(null);
        setThumb(null);

    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        getDrawingRect(drawingRect);
        paint.setColor(Color.DKGRAY);
        paint.setAntiAlias(false);
        int margin = ThumbWidth / 2;
        for(int i = margin; i < drawingRect.width() - margin; i+= TicSpacingPrimary){
            canvas.drawLine(i, drawingRect.bottom - 2, i, drawingRect.bottom - (TicLengthPrimary -2), paint);
        }

        paint.setColor(Color.GRAY);
        for(int i = margin; i < drawingRect.width() - margin; i+= TicSpacingSecondary){
            canvas.drawLine(i, drawingRect.bottom, i, drawingRect.bottom - TicLengthSecondary, paint);
        }


        for(int i = margin; i < drawingRect.width() - margin; i+= TicSpacingTertiary){
            canvas.drawLine(i, drawingRect.bottom, i, drawingRect.bottom - TicLengthTertiary, paint);
        }

        int progress = (int)((getProgress() * 0.01) * (drawingRect.width() - ThumbWidth) + margin);
        thumbRect.set(progress - ThumbWidth / 2, 2, progress + ThumbWidth / 2, 15);
        paint.setColor(0xff00DDFF);
        canvas.drawRect(thumbRect, paint);

        Path path = new Path();
        path.moveTo(thumbRect.left, thumbRect.bottom);
        path.lineTo(thumbRect.centerX(), thumbRect.bottom + 10);
        path.lineTo(thumbRect.right, thumbRect.bottom);
        paint.setAntiAlias(true);
        canvas.drawPath(path, paint);

        paint.setTextSize(12f);
        String text = getProgress() + "%";
        paint.getTextBounds(text, 0, text.length(), textRect);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setTypeface(typeface);
        canvas.drawText(text, progress - textRect.centerX(), textRect.height() + 5, paint);

        Object tag = getTag();
        if(tag != null){
            String label = (String)tag;
            paint.setColor(LabelColor);
            paint.setTextSize(10f);
            paint.getTextBounds(label, 0, label.length(), textRect);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(1f, 1f, textRect.width() + 15f, textRect.height() + 10f, paint);

            paint.setAntiAlias(true);
            //draw the flair on the end
            path = new Path();
            path.moveTo(textRect.width() + 15f, textRect.height() + 10f);
            path.lineTo(textRect.width() + 30f, 1f);
            path.lineTo(textRect.width() + 15f, 1f);
            canvas.drawPath(path, paint);

            paint.setColor(Color.BLACK);
            canvas.drawText(label, 10f, textRect.height() + 5, paint);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(false);
        paint.setColor(Color.WHITE);
        drawingRect.set(drawingRect.left, drawingRect.top, drawingRect.right - 1, drawingRect.bottom - 1);
        canvas.drawRect(drawingRect, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private Rect drawingRect = new Rect();
    private Rect thumbRect = new Rect();
    private Rect textRect = new Rect();
    private Paint paint = new Paint();
    private static final int TicSpacingPrimary = 2;
    private static final int TicLengthPrimary = 7;

    private static final int TicSpacingSecondary = 56;
    private static final int TicLengthSecondary = 12;

    private static final int TicSpacingTertiary = 28;
    private static final int TicLengthTertiary = 7;
    private static final int ThumbWidth = 30;
    private static Typeface typeface;

    private static final int LabelColor = Color.argb(64, 255, 255, 255);
}
