package com.blindstick.el213_grp3.trackme;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ActionMenuItemView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Button btn_sendLocation;
    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_sendLocation = (Button) findViewById(R.id.btn_sendLocation);

        btn_sendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(ActivityCompat.checkSelfPermission(MainActivity.this,mPermission)!= PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{mPermission},REQUEST_CODE_PERMISSION);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                gps = new GPSTracker(MainActivity.this);

                if (gps.canGetLocation()) {
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    long time = gps.getTime();

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(time);
                    Date date = calendar.getTime();

                    Toast.makeText(getApplicationContext(), "Your location is - \nLat: " + latitude + "\nLong: " + longitude +
                                "\nRecorded at: " + date.toString(), Toast.LENGTH_LONG).show();
                } else {
                    gps.showSettingsAlert();
                }
            }
        });
    }
}
