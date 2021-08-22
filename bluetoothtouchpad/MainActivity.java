package com.example.bluetoothtouchpad;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ConnectDialog.ConnectDialogListener {

    BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothSocket bSocket = null;
    private BluetoothDevice choosenDevice = null;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");        //UUID of serial port

    private Menu menu;                                  //Custom toolbar
    private ConnectDialog dialog;                       //Custom dialog

    private boolean onBlueClicked = false;              //Bluetooth flag
    private boolean stream_exist = false;               //Stream flag
    private static final int REQUEST_ENABLE_BT = 0;     //BT flag

    // Definition of commands
    static final String LEFT_CLICK = "LMB";
    static final String RIGHT_CLICK = "RMB";
    static final String MOVE_CURSOR = "MOV:";
    static final String END_OF_CLICK = "EOC";
    static final String LONG_LEFT_CLICK = "LLC";
    static final String CENTRAL_SCROLL_CLICK = "CSC";

    //Buttons
    Button leftButton;
    Button rightButton;
    Button centralButton;

    //Array definitions
    ArrayList <BluetoothDevice> BoundedDevices = new ArrayList<>();
    ArrayList <BluetoothDevice> FoundDevices = new ArrayList<>();

    //Additional thread
    ConnectedThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar my_toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(my_toolbar);
        getSupportActionBar().setTitle(getString(R.string.toolbar_title));

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0x1);
        }
        dialog = new ConnectDialog();
        dialog.setbAdapter(bAdapter);

        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);
        centralButton = findViewById(R.id.centralButton);

        initButtons();
    }

    //Discovering handling method
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();

                if (deviceName != null) {
                    Log.d("FD", deviceName);
                    FoundDevices.add(device);
                    showToast("Found "+ deviceName,true);
                }
            }

            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                Log.d("End","Finished discovery...");
                Log.d("",String.valueOf(onBlueClicked));
                if (onBlueClicked && bAdapter.isEnabled()) {
                    checkLists();
                    openDialog();
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);

        this.menu = menu;

        if (!bAdapter.isEnabled()) {
            menu.findItem(R.id.BLT).setIcon(R.drawable.ic_baseline_bluetooth_disabled_24);
            onBlueClicked = false;
            stream_exist = false;
        }
        else {
            menu.findItem(R.id.BLT).setIcon(R.drawable.ic_baseline_bluetooth_24);
            FoundDevices.clear();
            onBlueClicked = true;
            getBoundenDevice();
            if(bAdapter.isDiscovering())
                bAdapter.cancelDiscovery();
            bAdapter.startDiscovery();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId()== R.id.BLT) {
            Log.i("TAG", "Turning bt clicked...");

            if (!bAdapter.isEnabled()) {
                onBlueClicked = true;
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }

            if (bAdapter.isEnabled()) {
                onBlueClicked = false;
                stream_exist = false;
                showToast("Turning off Bluetooth...",false);
                bAdapter.disable();
                menu.findItem(R.id.BLT).setIcon(R.drawable.ic_baseline_bluetooth_disabled_24);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT)
        {
            if (resultCode == RESULT_OK)
            {
                showToast("Bluetooth is on...",false);
                onBlueClicked = true;
                getBoundenDevice();
                FoundDevices.clear();
                if(bAdapter.isDiscovering())
                    bAdapter.cancelDiscovery();
                bAdapter.startDiscovery();
                menu.findItem(R.id.BLT).setIcon(R.drawable.ic_baseline_bluetooth_24);
            }

            else
            {
                showToast("Bluetooth is off...",false);
                onBlueClicked = false;
                menu.findItem(R.id.BLT).setIcon(R.drawable.ic_baseline_bluetooth_disabled_24);
                stream_exist = false;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // method to show Toast
    // txt - text of message to be displayed in Toast
    // duration_long - if true, duration of Toast is longer
    protected void showToast(CharSequence txt, boolean duration_long)
    {
        if (duration_long)
            Toast.makeText(this,txt,Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this,txt,Toast.LENGTH_SHORT).show();
    }

    //Destructor
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
    
    private void openDialog()
    {
        dialog.show(getSupportFragmentManager(),"Dialog");
    }

    //Method returning an array of paired bl devices
    public void getBoundenDevice()
    {
        Set<BluetoothDevice> pairedDevices = bAdapter.getBondedDevices();
        if (pairedDevices.size() > 0)
        {
            BoundedDevices.clear();
            BoundedDevices.addAll(pairedDevices);
        }
    }

    //Method checking if any of the paired device is available
    public void checkLists()
    {
        dialog.MatchingDevicesAddress.clear();
        dialog.MatchingDevicesName.clear();

        int size = BoundedDevices.size();
        int f_size = FoundDevices.size();

        for (int i = 0; i < size; i++ )
        {
            for (int j = 0; j < f_size; j++)
            {
                // If name of both devices matches add them to the array
                if (BoundedDevices.get(i).getName().equals(FoundDevices.get(j).getName()))
                {
                    Log.d("","Is matching: " + BoundedDevices.get(i).getName());
                    dialog.MatchingDevicesName.add(BoundedDevices.get(i).getName());
                    dialog.MatchingDevicesAddress.add(BoundedDevices.get(i).getAddress());
                }
                //If not create log
                else
                {
                    Log.d("","Is not");
                }
            }
        }
    }

    //Additional thread
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
        }

        public void run() {
            bAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                stream_exist = true;
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    stream_exist = false;
                } catch (IOException closeException) {
                    Log.e("", "Could not close the client socket", closeException);
                }
            }
        }

        public void write(String msg) {
            if (stream_exist) {
                try {
                    //Add end line char to message so the server can receive message
                    msg = msg + "\n";
                    mmSocket.getOutputStream().write(msg.getBytes());
                } catch (IOException e) {
                    Log.e("", "Error occurred when sending data", e);
                }
            }
        }
    }

    //Method that create socket
    public void createSocket(BluetoothDevice device)
    {
        try {
            bSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            Log.d("BSocket","Socket created successfully");
        } catch (IOException e) {
            Log.e("", "Socket's create() method failed", e);
        }
    }

    //Get attributes from ConnectDialog
    @Override
    public void sendAttributes(String dName, String Address)
    {
        showToast(dName + ", " + Address, false);

        for (BluetoothDevice device : FoundDevices)
        {
            Log.d("","Name: " + dName + " to " + device.getName());
            if(device.getName().equals(dName) && device.getAddress().equals(Address)) {
                this.choosenDevice = device;
                break;
            }
        }

        if (choosenDevice != null)
        {
            Log.d("","Entering socket creation...");
            createSocket(choosenDevice);
            thread = new ConnectedThread(bSocket);
            thread.start();
        }
        else
            Log.d("Socket","Socket not created, connectivity failed");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (bSocket != null && bAdapter.isEnabled()) {
            if (bSocket.isConnected()) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                switch (event.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("TAG", "touched down");
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (thread != null)
                        {
                            thread.write(MOVE_CURSOR+x+","+y);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        Log.d("TAG", "touched up");
                        if (thread != null)
                        {
                            thread.write(END_OF_CLICK);
                        }
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        Log.d("","Pointer down");
                        if (thread != null)
                        {
                            thread.write(LONG_LEFT_CLICK);
                        }
                        break;
                }
            }
        }
        return true;
    }

    //Initialize buttons
    private void initButtons()
    {
        leftButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (thread != null) {
                    thread.write(LEFT_CLICK);
                }
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (thread != null)
                    thread.write(RIGHT_CLICK);
            }
        });

        centralButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (thread != null)
                    thread.write(CENTRAL_SCROLL_CLICK);
            }
        });
    }
}