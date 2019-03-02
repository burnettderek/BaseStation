package com.decibel.civilianc2.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.libcivilian.R;

/**
 * Created by dburnett on 6/5/2018.
 */

public class NumericTouchControl extends LinearLayout implements View.OnClickListener {

    public interface OnRequestChangeListener {
        void onRequestChange(int increment);
    }

    public NumericTouchControl(Context context) {
        super(context);
        init(context);
    }

    public NumericTouchControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NumericTouchControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setIncrement(int multiple){
        this.increment = multiple;
    }

    private void init(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.numeric_touch_control, this);
        upButton = this.findViewById(R.id.btnUp);
        downButton = this.findViewById(R.id.btnDown);
        lblDigitLarge = this.findViewById(R.id.lblDigitLarge);
        lblDigitSmall = this.findViewById(R.id.lblDigitSmall);
        lblDigitTiny = this.findViewById(R.id.lblDigitTiny);
        upButton.setOnClickListener(this);
        downButton.setOnClickListener(this);
        lblDigitSmall.setVisibility(INVISIBLE);
        lblDigitTiny.setVisibility(INVISIBLE);
    }

    public void setValue(char value){
        lblDigitTiny.setVisibility(INVISIBLE);
        lblDigitSmall.setVisibility(INVISIBLE);
        lblDigitLarge.setVisibility(VISIBLE);
        this.lblDigitLarge.setText(new String() + value);
    }

    public void setValue(int value){
        if(value > 9){
            lblDigitLarge.setVisibility(INVISIBLE);
            lblDigitSmall.setVisibility(VISIBLE);
            lblDigitTiny.setVisibility(INVISIBLE);
            lblDigitSmall.setText(Integer.toString(value));
        } else {
            lblDigitTiny.setVisibility(INVISIBLE);
            lblDigitSmall.setVisibility(INVISIBLE);
            lblDigitLarge.setVisibility(VISIBLE);
            lblDigitLarge.setText(Integer.toString(value));
        }
    }

    public void setValue(double value){
        lblDigitLarge.setVisibility(INVISIBLE);
        lblDigitSmall.setVisibility(INVISIBLE);
        lblDigitTiny.setVisibility(VISIBLE);
        lblDigitTiny.setText(String.format("%.01f", value));
    }

    public void setOnRequestChangeListener(OnRequestChangeListener listener){
        this.changeListener = listener;
    }

    @Override
    public void onClick(View v) {
        if(changeListener != null){
            if(v == upButton){
                changeListener.onRequestChange(increment);
            } else if (v == downButton){
                changeListener.onRequestChange(-increment);
            }
        }
    }

    private int increment;
    private OnRequestChangeListener changeListener;
    private Button upButton;
    private Button downButton;
    private TextView lblDigitLarge;
    private TextView lblDigitSmall;
    private TextView lblDigitTiny;
}
