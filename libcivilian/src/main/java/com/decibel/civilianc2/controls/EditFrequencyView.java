package com.decibel.civilianc2.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.libcivilian.R;

import java.util.ArrayList;
import java.util.List;

public class EditFrequencyView extends LinearLayout implements NumericTouchControl.OnRequestChangeListener {
    public EditFrequencyView(Context context) {
        super(context);
        init(context);
    }

    public EditFrequencyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EditFrequencyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public interface OnTuneListener {
        void onTuneRequested(int increment);
    }

    private void init(Context context){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.editfrequency_view, this);
        hundreds = this.findViewById(R.id.numericTouchControl);
        hundreds.setIncrement(100000);
        tens = this.findViewById(R.id.numericTouchControl2);
        tens.setIncrement(10000);
        ones = this.findViewById(R.id.numericTouchControl3);
        ones.setIncrement(1000);
        tenths = this.findViewById(R.id.numericTouchControl4);
        tenths.setIncrement(100);
        hundreths = this.findViewById(R.id.numericTouchControl5);
        hundreths.setIncrement(10);
        thousandths = this.findViewById(R.id.numericTouchControl6);
        thousandths.setIncrement(1);

        hundreds.setOnRequestChangeListener(this);
        tens.setOnRequestChangeListener(this);
        ones.setOnRequestChangeListener(this);
        tenths.setOnRequestChangeListener(this);
        hundreths.setOnRequestChangeListener(this);
        thousandths.setOnRequestChangeListener(this);
    }

    public void setFrequencyDisplay(int frequency){
        char[] digits = getDigits(frequency);
        hundreds.setValue(digits[0]);
        tens.setValue(digits[1]);
        ones.setValue(digits[2]);
        tenths.setValue(digits[3]);
        hundreths.setValue(digits[4]);
        thousandths.setValue(digits[5]);
    }

    public void setOnTuneListener(OnTuneListener listener){
        this.listener = listener;
    }

    @Override
    public void onRequestChange(int increment) {
        if(this.listener != null){
            this.listener.onTuneRequested(increment);
        }
    }

    private static char[] getDigits(int num) {
        String form = String.format("%06d", num);
        return form.toCharArray();
    }

    private NumericTouchControl hundreds;
    private NumericTouchControl tens;
    private NumericTouchControl ones;
    private NumericTouchControl tenths;
    private NumericTouchControl hundreths;
    private NumericTouchControl thousandths;

    private OnTuneListener listener;
}
