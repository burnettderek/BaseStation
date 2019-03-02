package com.decibel.civilianc2.controls;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.example.libcivilian.R;


/**
 * Created by dburnett on 3/22/2018.
 */

public class StyledRadioButton extends RadioButton {
    public StyledRadioButton(Context context) {
        super(context);
        init(context);
    }

    public StyledRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StyledRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public StyledRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context){
        try {
            if (typeface == null) {
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Exo2-Regular.ttf");
            }
        }catch (Exception e){
            typeface = Typeface.DEFAULT;
        }
        this.setTypeface(typeface);
        this.setTextColor(Color.LTGRAY);
        this.setButtonDrawable(R.drawable.unchecked);
        this.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    setButtonDrawable(R.drawable.checked);
                    setTextColor(Color.WHITE);
                }
                else {
                    setButtonDrawable(R.drawable.unchecked);
                    setTextColor(Color.LTGRAY);
                }
            }
        });
    }

    private static Typeface typeface;
}
