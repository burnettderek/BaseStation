package com.decibel.civilianc2;

import android.app.Application;

import com.decibel.civilianc2.model.managers.Model;
import com.decibel.civilianc2.radios.RadioDriverFactory;

/**
 * Created by dburnett on 1/11/2018.
 */

public class CommandCenterApplication extends Application {
    public void onCreate(){
        super.onCreate();
        Model.onCreate(this);
        RadioDriverFactory.setContext(this);
        RadioDriverFactory.getInstance().setPersistence(Model.getInstance().getUserSettings());
        Model.getInstance().setTransceiver(RadioDriverFactory.getInstance().getTransceiver(RadioDriverFactory.DRA818V_DRIVER_KEY));
    }
}
