package com.blindstick.el213_grp3.trackme;

import android.app.Application;

import com.firebase.client.Firebase;
import com.firebase.client.core.Context;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
