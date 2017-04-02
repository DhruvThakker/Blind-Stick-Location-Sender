package com.blindstick.el213_grp3.trackme;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v7.view.menu.ActionMenuItemView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

import static com.blindstick.el213_grp3.trackme.RegisterActivity.MY_PREFS_NAME;

public class MainActivity extends AppCompatActivity {

    Button btn_showTrackingId, btn_sendLocation;
    GPSTracker gps;
    Firebase Ref, UserIdRef;
    String name = null, trackingId = null;
    int year;
    double latitude, longitude;
    long time, mob1, mob2;
    private String appURL = "http://blindstick.el213grp3.sticklocater.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Ref = new Firebase("https://track-me-63d56.firebaseio.com/");

        Bundle bundle = getIntent().getExtras();
        name = (String) bundle.get("name");
        year = (Integer) bundle.get("year");
        mob1 = (Long) bundle.get("mob1");
        mob2 = (Long) bundle.get("mob2");
        trackingId = name + year;

        UserIdRef = Ref.child(trackingId);
        UserIdRef.child("Name").setValue(name);
        UserIdRef.child("Year").setValue(year);
        UserIdRef.child("Mob1").setValue(mob1);
        UserIdRef.child("Mob2").setValue(mob2);
        if((Boolean) bundle.get("first"))
            setBtn_sendLocation();

        btn_showTrackingId = (Button) findViewById(R.id.btn_trackingId);
        btn_showTrackingId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTrackingId();
            }
        });

        btn_sendLocation = (Button) findViewById(R.id.btn_sendLocation);
        btn_sendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mobN1 = Long.toString(mob1);
                String mobN2 = Long.toString(mob2);
                SmsManager.getDefault()
                        .sendTextMessage(mobN1, null, "This is " + name + ". I am in trouble. Need Help. You can Locate me with my tracking id: "+name+year+ " at "+appURL+trackingId, null, null);
                SmsManager.getDefault()
                        .sendTextMessage(mobN2, null, "This is " + name + ". I am in trouble. Need Help. You can Locate me with my tracking id: "+name+year+ " at "+appURL+trackingId, null, null);
                setBtn_sendLocation();
            }
        });
    }

    private void setBtn_sendLocation() {
        gps = new GPSTracker(MainActivity.this);

        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            time = gps.getTime();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            Date date = calendar.getTime();
            if (latitude != 0) {
                UserIdRef.child("Latitude").setValue(latitude);
                UserIdRef.child("Longitude").setValue(longitude);
                UserIdRef.child("Time").setValue(time);
                Toast.makeText(getApplicationContext(), "Your location is - \nLat: " + latitude + "\nLong: " + longitude +
                        "\nRecorded at: " + date.toString(), Toast.LENGTH_LONG).show();
            }

        } else {
            gps.showSettingsAlert();
        }
    }

    public void showTrackingId() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Tracking ID");

        alertDialog.setMessage(name + year);

        alertDialog.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri uri = Uri.parse("smsto:");
                Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                it.putExtra("sms_body", "This is " + name + ". You can track my location using tracking id: " + name + year + " using Stick Locator App.");
                startActivity(it);

            }
        });

        alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }
}
