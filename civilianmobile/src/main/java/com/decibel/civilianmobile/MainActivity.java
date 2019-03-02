package com.decibel.civilianmobile;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.decibel.civilianc2.activities.ConfigureRadio;
import com.decibel.civilianc2.activities.RadioControl;
import com.decibel.civilianc2.activities.ViewStations;
import com.decibel.civilianc2.model.managers.Model;
import com.decibel.civilianc2.model.managers.PositionBeacon;
import com.decibel.civilianc2.radios.RSUV3;
import com.decibel.civilianmobile.activities.MapActivity;
import com.decibel.civilianmobile.activities.Settings;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button)findViewById(R.id.btnMap);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnDoMap();
            }
        });

        Button radio = findViewById(R.id.btnConfigRadio);
        radio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnDoRadio();
            }
        });

        Button settings = findViewById(R.id.btnSettings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSettings();
            }
        });

        Button stations = findViewById(R.id.btnViewStations);
        stations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewStations();
            }
        });

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        2);
            }
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        PositionBeacon beacon = new PositionBeacon(locationManager, Model.getInstance().getUserSettings(),
                                                   Model.getInstance().getStationManager(), Model.getInstance().getPositionManager(),
                                                   Model.getInstance().getModulator());
        Model.getInstance().setPositionBeacon(beacon);


        UsbManager manager = (UsbManager)getSystemService(Context.USB_SERVICE);

        UsbReciever usbReciever = new UsbReciever();
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReciever, filter);
        UsbDevice device = RSUV3.getUsbDevice(manager);
        if(device != null) {
            manager.requestPermission(device, mPermissionIntent);
            boolean hasPermision = manager.hasPermission(device);

            UsbDeviceConnection connection = manager.openDevice(device);
        }
    }

    private void OnDoRadio() {
        startActivity(new Intent(this, RadioControl.class));
    }

    private void onSettings() {
        startActivity(new Intent(this, Settings.class));
    }


    protected void OnDoMap(){
        startActivity(new Intent(this, MapActivity.class));
    }

    protected void onViewStations() { startActivity(new Intent(this, ViewStations.class));}

    class UsbReciever extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action))
            {
                synchronized (this)
                {
                    UsbDevice device = (UsbDevice)intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    {
                        if (device != null)
                        {
                            // call method to set up device communication
                        }
                    }
                    else
                    {

                    }
                }
            }
        }
    }
    private static String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
}
