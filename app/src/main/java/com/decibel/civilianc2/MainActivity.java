package com.decibel.civilianc2;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.decibel.civilianc2.activities.ConfigureRadioTVDisplay;
import com.decibel.civilianc2.activities.FSKModemTest;
import com.decibel.civilianc2.activities.MapActivity;
import com.decibel.civilianc2.activities.Settings;
import com.decibel.civilianc2.radios.RadioDriverFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

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

        button = (Button)findViewById(R.id.btnConfigRadio);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnDoRadios();
            }
        });

        Button settings = findViewById(R.id.btnSettings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSettings();
            }
        });

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());


        ((TextView)findViewById(R.id.txtIPAddress)).setText(ip);
    }

    private void onSettings() {
        startActivity(new Intent(this, Settings.class));
    }

    protected void OnDoFSKModemTest()
    {
        startActivity(new Intent(this, FSKModemTest.class));
    }

    protected void OnDoMap(){
        startActivity(new Intent(this, MapActivity.class));
    }

    protected void OnDoRadios() {
        Intent intent = new Intent(this, ConfigureRadioTVDisplay.class);
        intent.putExtra("radio.driver", RadioDriverFactory.RS_UV3A_DRIVER_KEY);
        startActivity(intent);
    }

    protected void copyAssets(){
        AssetManager assetManager = getApplicationContext().getAssets();
        try {

            String target = Environment.getExternalStorageDirectory().getPath() + "/osmdroid/WarRoomDefault.sqlite";
            File dir = new File(Environment.getExternalStorageDirectory(), "osmdroid");
            if(!dir.exists())
                dir.mkdirs();
            copyAssetFile("WarRoomDefault.sqlite", target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void copyAssetFile(String assetFilePath, String destinationFilePath) throws IOException
    {
        File isAlreadyThere = new File(destinationFilePath);
        if(isAlreadyThere.exists())isAlreadyThere.delete();
        InputStream in = getApplicationContext().getAssets().open(assetFilePath);
        OutputStream out = new FileOutputStream(destinationFilePath);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        in.close();
        out.close();
    }
}
