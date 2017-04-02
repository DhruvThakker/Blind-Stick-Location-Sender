package com.blindstick.el213_grp3.trackme;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class RegisterActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION_MSG = 2;
    EditText et_name, et_year, et_mob1, et_mob2;
    Button btn_register;
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    String name = null;
    int year;
    long mob1, mob2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        checkForPermisssions();

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        name = prefs.getString("name", null);
        year = prefs.getInt("year", 0);
        mob2 = prefs.getLong("mob2", 0);
        mob1 = prefs.getLong("mob1", 0);
        btn_register = (Button) findViewById(R.id.btn_register);
        if (!(name == null || year == 0 || mob1 == 0 || mob2 == 0)) {

            Intent i = new Intent(RegisterActivity.this, MainActivity.class);
            i.putExtra("name", name);
            i.putExtra("year", year);
            i.putExtra("mob1", mob1);
            i.putExtra("mob2", mob2);
            i.putExtra("first",false);
            finish();
            startActivity(i);
        }
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                et_name = (EditText) findViewById(R.id.et_name);
                et_year = (EditText) findViewById(R.id.et_year);
                et_mob1 = (EditText) findViewById(R.id.et_mob1);
                et_mob2 = (EditText) findViewById(R.id.et_mob2);

                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                if (et_name.getText().toString().isEmpty() || et_year.getText().toString().isEmpty() || et_mob1.getText().toString().isEmpty() || et_mob2.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                } else if (!et_name.getText().toString().matches("[A-Za-z]+")) {
                    Toast.makeText(RegisterActivity.this, "Please enter name with alphabets only", Toast.LENGTH_SHORT).show();
                } else if (!et_year.getText().toString().matches("[0-9]+")) {
                    Toast.makeText(RegisterActivity.this, "Please enter year in numbers only", Toast.LENGTH_SHORT).show();
                } else if (!et_mob1.getText().toString().matches("[0-9]+") || !et_mob2.getText().toString().matches("[0-9]+")) {
                    Toast.makeText(RegisterActivity.this, "Please enter valid mobile number", Toast.LENGTH_SHORT).show();
                } else {
                    name = et_name.getText().toString();
                    year = Integer.parseInt(et_year.getText().toString());
                    mob1 = Long.parseLong(et_mob1.getText().toString());
                    mob2 = Long.parseLong(et_mob2.getText().toString());

                    editor.putString("name", name);
                    editor.putInt("year", year);
                    editor.putLong("mob1", mob1);
                    editor.putLong("mob2", mob2);
                    editor.commit();

                    String mobN1 = Long.toString(mob1);
                    String mobN2 = Long.toString(mob2);
                    SmsManager.getDefault().sendTextMessage(mobN1, null, "This is " + name + ". You can track my location using tracking id: " + name + year + " using Stick Locator App.", null, null);
                    SmsManager.getDefault().sendTextMessage(mobN2, null, "This is " + name + ". You can track my location using tracking id: " + name + year + " using Stick Locator App.", null, null);

                    Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                    i.putExtra("name", name);
                    i.putExtra("year", year);
                    i.putExtra("mob1", mob1);
                    i.putExtra("mob2", mob2);
                    i.putExtra("first",true);
                    finish();
                    startActivity(i);
                }
            }

        });

    }

    private void checkForPermisssions() {
        try {
            if (ActivityCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{android.Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION_MSG);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
