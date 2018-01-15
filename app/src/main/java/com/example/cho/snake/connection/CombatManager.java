package com.example.cho.snake.connection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;

import com.example.cho.snake.MainActivity;

/**
 * Created by cho on 18. 1. 10.
 */

public class CombatManager {
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private Activity mActivity;
    static final public int REQUEST_ENABLE_BT = 100;
    static final public int REQUEST_CONNECT_DEVICE = 101;

    public CombatManager(Activity activity, Handler handler) {
        mHandler = handler;
        mActivity = activity;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private boolean getDeviceState() {
        return (mBluetoothAdapter != null);
    }

    private void enableBluetooth() {
        if(!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(intent, REQUEST_ENABLE_BT);
        } else {
            scanDevice();
        }
    }

    public void initConnection() {
        if(getDeviceState()) {
            enableBluetooth();
        }
    }

    public void scanDevice() {
        ((MainActivity)mActivity).showDevicesListState();
    }
}
