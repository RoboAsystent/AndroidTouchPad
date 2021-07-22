package com.example.bluetoothtouchpad;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatDialogFragment;

import java.lang.annotation.Inherited;
import java.util.Set;

public class ConnectDialog extends AppCompatDialogFragment {

    public BluetoothAdapter bAdapter;

    public void setbAdapter(BluetoothAdapter bAdapter) {
        this.bAdapter = bAdapter;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        builder.setView(view)
                .setTitle("Devices list")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        showBoundenDevice();
        return builder.create();
    }

    public void showBoundenDevice()
    {
        Set<BluetoothDevice> pairedDevices = bAdapter.getBondedDevices();
        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                String deviceName = device.getName();
                Log.d("Paired device name: ",deviceName);
            }
        }
    }
}
