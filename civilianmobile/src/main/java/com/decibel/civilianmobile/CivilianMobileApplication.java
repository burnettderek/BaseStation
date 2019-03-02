package com.decibel.civilianmobile;

import android.app.Application;

import com.decibel.civilianc2.model.managers.Model;
import com.decibel.civilianc2.radios.MockRadio;

/**
 * Created by dburnett on 3/30/2018.
 */

public class CivilianMobileApplication extends Application {
    public void onCreate(){
        super.onCreate();
        Model.onCreate(this);
        Model.getInstance().setTransceiver(new MockRadio());
    }
}
