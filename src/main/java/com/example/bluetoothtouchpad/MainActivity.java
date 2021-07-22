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
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.OutputStream;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket bSocket = null;
    private OutputStream oStream = null;

    private Menu menu;
    private static final int REQUEST_ENABLE_BT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar my_toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(my_toolbar);
        getSupportActionBar().setTitle(getString(R.string.toolbar_title));

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(receiver, filter);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0x1);
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                if (deviceName != null)
                    Log.d("Found device: ",deviceName);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);

        this.menu = menu;

        if (!bAdapter.isEnabled())
            menu.findItem(R.id.BLT).setIcon(R.drawable.ic_baseline_bluetooth_disabled_24);
        else {
            menu.findItem(R.id.BLT).setIcon(R.drawable.ic_baseline_bluetooth_24);
            //showBoundenDevice(bAdapter);
            openDialog();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId()== R.id.BLT) {
            Log.i("TAG", "Turning bt clicked...");

            if (!bAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }

            if (bAdapter.isEnabled()) {
                showToast("Turning off Bluetooth...");
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
                showToast("Bluetooth is on...");
                //showBoundenDevice(bAdapter);
                openDialog();
                if(bAdapter.isDiscovering())
                    bAdapter.cancelDiscovery();
                bAdapter.startDiscovery();
                menu.findItem(R.id.BLT).setIcon(R.drawable.ic_baseline_bluetooth_24);
            }

            else
            {
                showToast("Bluetooth is off...");
                menu.findItem(R.id.BLT).setIcon(R.drawable.ic_baseline_bluetooth_disabled_24);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int x = -1;
        int y = -1;

        x = (int) event.getX();
        y = (int) event.getY();

        switch (event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                Log.d("TAG", "touched down");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("TAG", "moving: (" + x + ", " + y + ")");
                break;
            case MotionEvent.ACTION_UP:
                Log.d("TAG", "touched up");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
        }
        return true;
    }

    protected void showToast(CharSequence txt)
    {
        Toast.makeText(this,txt,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
    
    private void openDialog()
    {
        ConnectDialog dialog = new ConnectDialog();
        dialog.setbAdapter(bAdapter);
        dialog.show(getSupportFragmentManager(),"Dialog");
    }
}