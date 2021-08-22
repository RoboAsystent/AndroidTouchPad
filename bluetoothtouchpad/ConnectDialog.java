package com.example.bluetoothtouchpad;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatDialogFragment;
import java.util.ArrayList;


public class ConnectDialog extends AppCompatDialogFragment {

    public BluetoothAdapter bAdapter;

    public final ArrayList<String> MatchingDevicesName = new ArrayList<String>();
    public final ArrayList<String> MatchingDevicesAddress = new ArrayList<String>();

    private ConnectDialogListener listener;

    //Setting the bluetooth adapter from main activity
    public void setbAdapter(BluetoothAdapter bAdapter) {
        this.bAdapter = bAdapter;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        if (MatchingDevicesName.size() == 0)
        {
           MatchingDevicesName.add("Nie odnaleziono pasującego urządzenia");
           MatchingDevicesAddress.add("0");
        }

        Spinner spinnerDevices = view.findViewById(R.id.spinner_blt);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, MatchingDevicesName);
        spinnerDevices.setAdapter(adapter);

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
                        int size = MatchingDevicesName.size();
                        Log.d("Matching size is: ", String.valueOf(size));
                        int idx = spinnerDevices.getSelectedItemPosition();
                        listener.sendAttributes(MatchingDevicesName.get(idx), MatchingDevicesAddress.get(idx));
                        MatchingDevicesAddress.clear();
                        MatchingDevicesName.clear();
                    }
                });
        return builder.create();
    }

    //Enable sending params to main activity
    public interface ConnectDialogListener{
        void sendAttributes(String dName, String Address);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ConnectDialogListener) context;
        }
        catch (ClassCastException e)
        {
               throw new ClassCastException(context.toString() +
                       "must implement ConnectDialogListener");
        }

    }
}