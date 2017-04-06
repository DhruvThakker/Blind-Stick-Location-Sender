package com.blindstick.el213_grp3.trackme;

/**
 * Created by Jay on 06-04-2017.
 */
        import android.app.Activity;
        import android.app.AlertDialog;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.net.Uri;
        import android.os.Handler;
        import android.os.Message;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.support.v7.widget.DefaultItemAnimator;
        import android.support.v7.widget.LinearLayoutManager;
        import android.support.v7.widget.RecyclerView;
        import android.telephony.SmsManager;
        import android.util.Log;
        import android.view.KeyEvent;
        import android.view.View;
        import android.view.inputmethod.EditorInfo;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.firebase.client.Firebase;

        import java.util.ArrayList;
        import java.util.Calendar;
        import java.util.Date;
        import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btn_showTrackingId, btn_sendLocation;
    GPSTracker gps;
    Firebase Ref, UserIdRef;
    String name = null, trackingId = null;
    int year;
    double latitude, longitude;
    long time, mob1, mob2;
    private String appURL = "http://blindstick.el213grp3.sticklocater.com/";
    Thread t;

    //---------------------------------
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;


    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private EditText mOutEditText;
    private Button mSendButton;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MessageAdapter mAdapter;
    private LinearLayoutManager linearLayoutManager;
    public int counter = 0;

    private List<com.blindstick.el213_grp3.trackme.Message> messageList = new ArrayList<com.blindstick.el213_grp3.trackme.Message>();
    //---------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---------------------------------
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MessageAdapter(getApplicationContext(),messageList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.scrollToPosition(mAdapter.getItemCount());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //---------------------------------

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

        /*btn_sendLocation = (Button) findViewById(R.id.btn_sendLocation);
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
        });*/

        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.arg1==1) {
                    gps = new GPSTracker(MainActivity.this);

                    if(gps.canGetLocation()) {

                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();
                        long time = gps.getTime();

                        UserIdRef.child("Latitude").setValue(latitude);
                        UserIdRef.child("Longitude").setValue(longitude);
                        UserIdRef.child("Time").setValue(time);
                    }
                }
                return false;
            }
        });

        t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000*60*45);
                        Message msg = new Message();
                        msg.arg1 = 1;
                        handler.sendMessage(msg);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        t.start();

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
                it.putExtra("sms_body", "This is " + name + ". You can track my location using tracking id: " + name + year + " at "+appURL+trackingId);
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

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }


    private void sendMessage(String message) {

        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    // If the action is a key-up event on the return key, send the message
                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        sendMessage(message);
                    }
                    return true;
                }
            };

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mAdapter.notifyDataSetChanged();
                    linearLayoutManager.scrollToPosition(mAdapter.getItemCount());
                    messageList.add(new com.blindstick.el213_grp3.trackme.Message(counter++, writeMessage, "Me"));
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (readMessage.equals("Y")){
                        String mobN1 = Long.toString(mob1);
                        String mobN2 = Long.toString(mob2);
                        SmsManager.getDefault()
                                .sendTextMessage(mobN1, null, "This is " + name + ". I am in trouble. Need Help. You can Locate me with my tracking id: "+name+year+ " at "+appURL+trackingId, null, null);
                        SmsManager.getDefault()
                                .sendTextMessage(mobN2, null, "This is " + name + ". I am in trouble. Need Help. You can Locate me with my tracking id: "+name+year+ " at "+appURL+trackingId, null, null);
                        setBtn_sendLocation();
                    }
                    mAdapter.notifyDataSetChanged();
                    linearLayoutManager.scrollToPosition(mAdapter.getItemCount());
                    Log.e("Message read","*****************"+readMessage);
                    messageList.add(new com.blindstick.el213_grp3.trackme.Message(counter++, readMessage, mConnectedDeviceName));
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    public void connect(View v) {
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    public void discoverable(View v) {
        ensureDiscoverable();
    }
}
