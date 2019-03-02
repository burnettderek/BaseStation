package com.decibel.civilianc2.controls;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by dburnett on 1/16/2018.
 */

public class AudioLevel extends View {

    public AudioLevel(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public AudioLevel(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public AudioLevel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        getDrawingRect(drawingRect);
        float ledSpacing = (float) (drawingRect.height() / (NumberOfLeds + 1));
        float ledHeight = ledSpacing - 3;
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)getLayoutParams();
        int ledWidth = params.width - params.rightMargin - params.leftMargin;
        paint.setColor(Color.GRAY);
        //paint.setStrokeWidth(5.0f);
        paint.setStrokeWidth(ledHeight);
        paint.setStyle(Paint.Style.STROKE);
        for(int i = 0; i < NumberOfLeds; i++){
            int index = NumberOfLeds - 1 - i;
            if (i < NumberOfLeds / 2) paint.setColor(Color.argb(255, 0, 100, 0));
            else if (i < NumberOfLeds * 0.75f) paint.setColor(Color.argb(255, 100, 100, 0));
            else paint.setColor(Color.argb(255, 100, 0, 0));
            //canvas.drawLine(params.leftMargin, (index * LedVerticalSpacing) + params.leftMargin + 10, params.leftMargin + ledWidth, (index * LedVerticalSpacing) + params.leftMargin + 10, paint);
            canvas.drawLine(params.leftMargin, (index * ledSpacing) + params.leftMargin + 10, params.leftMargin + ledWidth, (index * ledSpacing) + params.leftMargin + 10, paint);

        }

        for(int i = 0; i < NumberOfLeds; i++){
            int index = NumberOfLeds - 1 - i;
            float level = (float)i/(float)NumberOfLeds * 100.0f;
            if(level < audioLevel) {

                if (i < NumberOfLeds / 2) paint.setColor(Color.GREEN);
                else if (i < NumberOfLeds * 0.75f) paint.setColor(Color.YELLOW);
                else paint.setColor(Color.RED);
                canvas.drawLine(params.leftMargin, (index * ledSpacing) + params.leftMargin + 10, params.leftMargin + ledWidth, (index * ledSpacing) + params.leftMargin + 10, paint);

                paint.setMaskFilter(blurFilter);
                canvas.drawLine(params.leftMargin, (index * ledSpacing) + params.leftMargin + 10, params.leftMargin + ledWidth, (index * ledSpacing) + params.leftMargin + 10, paint);
                paint.setMaskFilter(null);
            }

        }
    }

    public void setAudioLevel(int level){
        audioLevel = level;
        this.invalidate();
    }


    private Paint paint = new Paint();

    private int audioLevel = 20;
    private int NumberOfLeds = 8;
    private int LedVerticalSpacing = 8;
    private MaskFilter blurFilter = new BlurMaskFilter(4.0f, BlurMaskFilter.Blur.OUTER);
    private Rect drawingRect = new Rect();
}
