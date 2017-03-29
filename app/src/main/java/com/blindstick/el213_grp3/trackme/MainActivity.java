package com.blindstick.el213_grp3.trackme;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btn_showTrackingId;
    String name=null, trackingId=null;
    int year;
    EditText et_name, et_year;
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        name = prefs.getString("name", null);
        year = prefs.getInt("year", 0);

        if(name==null || year==0 ){
            final Dialog dialog = new Dialog(this);

            dialog.setContentView(R.layout.custom_dialog);
            dialog.show();
            et_name = (EditText) dialog.findViewById(R.id.et_name);
            et_year = (EditText) dialog.findViewById(R.id.et_year);

            Button btn_ok = (Button) dialog.findViewById(R.id.btn_ok);

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    if(et_name.getText().toString().isEmpty() || et_year.getText().toString().isEmpty()){
                        Toast.makeText(MainActivity.this, "Please enter both details", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        name = et_name.getText().toString();
                        year = Integer.parseInt(et_year.getText().toString());
                        editor.putString("name", name);
                        editor.putInt("year", year);
                        editor.commit();
                        dialog.dismiss();
                    }

                }
            });
        }

        btn_showTrackingId = (Button) findViewById(R.id.btn_trackingId);
        btn_showTrackingId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTrackingId();
            }
        });
    }
    public void showTrackingId() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("Tracking ID");

        alertDialog.setMessage(name+year);

        alertDialog.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri uri = Uri.parse("smsto:");
                Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                it.putExtra("sms_body", "This is "+name+". You can track my location using tracking id: "+name+year+" using Stick Locator App.");
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