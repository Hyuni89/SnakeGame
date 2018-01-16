package com.example.cho.snake.connection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;

import com.example.cho.snake.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by cho on 18. 1. 10.
 */

public class CombatManager {
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private Activity mActivity;
    private int mState;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    static final public int REQUEST_ENABLE_BT = 100;
    static final public int REQUEST_CONNECT_DEVICE = 101;

    static final private int STATENONE = 200;
    static final private int STATELISTEN = 201;
    static final private int STATECONNECTING = 202;
    static final private int STATECONNECTED = 203;

    static final private UUID uuid = UUID.fromString("00000003-0000-1000-8000-00805F9B34FB");

    static final private int BUFSIZE = 1024;

    public CombatManager(Activity activity, Handler handler) {
        mHandler = handler;
        mActivity = activity;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setState(STATENONE);
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

    public void getDeviceInfo(Intent data) {
        String address = data.getExtras().getString(DeviceListFragment.EXTRADEVICEADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        connect(device);
    }

    private synchronized void setState(int state) {
        mState = state;
    }

    public synchronized void start() {
        if(mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if(mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    public synchronized void connect(BluetoothDevice device) {
        if(mState == STATECONNECTING) {
            if(mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if(mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATECONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if(mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if(mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        setState(STATECONNECTED);
    }

    public synchronized void stop() {
        if(mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if(mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATENONE);
    }

    public void write(byte[] out) {
        ConnectedThread r;
        synchronized(this) {
            if(mState != STATECONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        public ConnectThread(BluetoothDevice device) {
            mDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();

            try {
                mSocket.connect();
            } catch (IOException e) {
                setState(STATELISTEN);

                try {
                    mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                CombatManager.this.start();
                e.printStackTrace();

                return;
            }

            synchronized(CombatManager.this) {
                mConnectThread = null;
            }

            connected(mSocket, mDevice);
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInput;
        private final OutputStream mOutput;

        public ConnectedThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream tmpInput = null;
            OutputStream tmpOutput = null;

            try {
                tmpInput = mSocket.getInputStream();
                tmpOutput = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mInput = tmpInput;
            mOutput = tmpOutput;
        }

        public void run() {
            byte[] buffer = new byte[BUFSIZE];
            int bytes = 0;

            while(true) {
                try {
                    bytes = mInput.read(buffer);
                } catch (IOException e) {
                    setState(STATELISTEN);
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mOutput.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
