package com.blindstick.el213_grp3.trackme;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by honey on 3/29/2017.
 */

public class Blind_Stick extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
