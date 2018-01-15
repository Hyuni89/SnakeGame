package com.example.cho.snake.connection;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cho.snake.MainActivity;
import com.example.cho.snake.R;

import java.util.Set;

/**
 * Created by cho on 18. 1. 15.
 */

public class DeviceListFragment extends Fragment {

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mPairedAdapter;
    private ArrayAdapter<String> mNewAdapter;
    private Button mScanButton;
    private ListView mPairedListView;
    private ListView mNewListView;
    private ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.devicelist, container, false);

        mScanButton = (Button)view.findViewById(R.id.scanButton);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doDiscovery();
            }
        });

        mProgressBar = (ProgressBar)view.findViewById(R.id.progressBar);

        mPairedAdapter = new ArrayAdapter<String>(view.getContext(), R.layout.devicename);
        mNewAdapter = new ArrayAdapter<String>(view.getContext(), R.layout.devicename);

        mPairedListView = (ListView)view.findViewById(R.id.pairedDeviceList);
        mPairedListView.setAdapter(mPairedAdapter);
        mPairedListView.setOnItemClickListener(mDeviceClickListener);

        mNewListView = (ListView)view.findViewById(R.id.newDeviceList);
        mNewListView.setAdapter(mNewAdapter);
        mNewListView.setOnItemClickListener(mDeviceClickListener);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        ((MainActivity)getActivity()).registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        ((MainActivity)getActivity()).registerReceiver(mReceiver, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevice = mBluetoothAdapter.getBondedDevices();

        if(pairedDevice.size() > 0) {
            view.findViewById(R.id.pairedDevice).setVisibility(View.VISIBLE);
            for(BluetoothDevice device : pairedDevice) {
                mPairedAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevice = "No Devices Have Been Paired";
            mPairedAdapter.add(noDevice);
        }

        return view;
    }

    private void doDiscovery() {
        ((MainActivity)getActivity()).findViewById(R.id.newDevice).setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);

        if(mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        mBluetoothAdapter.startDiscovery();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mBluetoothAdapter.cancelDiscovery();

            String info = ((TextView)view).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent intent = new Intent();
            intent.putExtra("DeviceAddress", address);

            ((MainActivity)getActivity()).setResult(Activity.RESULT_OK, intent);
            ((MainActivity)getActivity()).finish();
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if(mNewAdapter.getCount() == 0) {
                    mProgressBar.setVisibility(View.GONE);
                    String noDevice = "No Device Found";
                    mNewAdapter.add(noDevice);
                }
            }
        }
    };
}
