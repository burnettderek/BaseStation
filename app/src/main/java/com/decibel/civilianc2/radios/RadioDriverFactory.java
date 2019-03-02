package com.decibel.civilianc2.radios;

import android.content.Context;

import com.decibel.civilianc2.model.dataaccess.UserSettings;
import com.decibel.civilianc2.model.managers.Model;

import java.util.HashMap;

/**
 * Created by dburnett on 2/22/2018.
 */

public class RadioDriverFactory {

    protected RadioDriverFactory(){
    }

    public static RadioDriverFactory getInstance(){
        if(instance == null){
            instance = new RadioDriverFactory();
        }
        return instance;
    }

    public static void setContext(Context context){
        RadioDriverFactory.context = context;
    }

    public void setPersistence(UserSettings userSettings){
        this.userSettings = userSettings;
    }

    public ITransceiver getTransceiver(String name){
        switch (name) {
            case DRA818V_DRIVER_KEY:
                if (dra818V == null)
                    dra818V = DRA818V.create(userSettings);
                return dra818V;
            case SA828V_DRIVER_KEY:
                if(sa828V == null)
                    sa828V = SA828V.create(userSettings);
                return sa828V;
            case MOCK_DRIVER_KEY:
                if (mockRadio == null)
                    mockRadio = new MockRadio();
                return mockRadio;
            case RS_UV3A_DRIVER_KEY:
                if(rsuv3 == null)
                    rsuv3 = new RSUV3(context);
                return rsuv3;
        }
        return null;
    }

    public final static String DRA818V_DRIVER_KEY = "DRA818V";
    public final static String MOCK_DRIVER_KEY =    "MOCK-7000";
    public final static String SA828V_DRIVER_KEY =  "SA828V";
    public final static String RS_UV3A_DRIVER_KEY = "RS-UV3A";
    private static RadioDriverFactory instance;
    private DRA818V dra818V;
    private SA828V sa828V;
    private RSUV3 rsuv3;
    private MockRadio mockRadio;
    private UserSettings userSettings;
    private static Context context;
}
