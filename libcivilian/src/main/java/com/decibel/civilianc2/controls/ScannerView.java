package com.decibel.civilianc2.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.decibel.civilianc2.radios.IReceiver;
import com.decibel.civilianc2.radios.Scanner;
import com.example.libcivilian.R;

import java.io.IOException;
import java.net.ConnectException;

/**
 * Created by dburnett on 2/28/2018.
 */

public class ScannerView extends LinearLayout{
    public ScannerView(final Context context) {
        super(context);
        init(context);
    }

    public ScannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ScannerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void setRadio(IReceiver radio){
        this.radio = radio;
    }

    public void init(final Context context){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.scanner_view, this);

        final Button scan = findViewById(R.id.btnScan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText txtScanFreq = findViewById(R.id.txtScanFreq);
                int freq = (int)(Double.parseDouble(txtScanFreq.getText().toString()) * 1000);
                try {
                    if (radio.scan(freq)) {
                        Toast.makeText(context, "Frequency detected.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "No frequency detected", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e){}
            }
        });

        ImageButton play = findViewById(R.id.btnStartRangeScan);
        play.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(scanner != null && scanner.getPaused()){
                        scanner.setPaused(false);
                        return;
                    } else if(scanner != null && scanner.getStopped()){
                        scanner.stop();
                    }
                    int startFreq = (int)(Double.parseDouble(((EditText) findViewById(R.id.scanStartFreqEdit)).getText().toString()) * 1000);
                    int stopFreq = (int)(Double.parseDouble(((EditText) findViewById(R.id.scanStopFrequencyEdit)).getText().toString()) * 1000);


                    scanner = new Scanner(getContext(), radio, freqStep, startFreq, stopFreq);
                    scanner.start();
                }
                catch (Exception e){
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        ImageButton pause = findViewById(R.id.btnPauseRangeScan);
        pause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(scanner != null)
                    scanner.setPaused(!scanner.getPaused());
            }
        });

        ImageButton stop = findViewById(R.id.btnStopRangeScan);
        stop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(scanner != null) {
                    scanner.stop();
                    scanner = null;
                }
            }
        });
    }

    private IReceiver radio;
    private Scanner scanner;
    private int freqStep = 10;
}
