package com.example.cho.snake.connection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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
    private AcceptThread mAcceptThread;

    static final public int REQUEST_ENABLE_BT = 100;
    static final public int REQUEST_CONNECT_DEVICE = 101;

    static final private int STATENONE = 200;
    static final private int STATELISTEN = 201;
    static final private int STATECONNECTING = 202;
    static final private int STATECONNECTED = 203;

//    static final private UUID uuid = UUID.fromString("00000000-0000-1000-8000-00805F9B34FB");
    private static final UUID uuid = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
//    private static final UUID uuid= UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    static final private String mRival = "Rival";

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

    private void userFeedback() {
        switch(mState) {
            case STATECONNECTED:
                Toast.makeText(mActivity, "Connected", Toast.LENGTH_SHORT).show();
                break;
            case STATECONNECTING:
                Toast.makeText(mActivity, "Connecting...", Toast.LENGTH_SHORT).show();
                break;
            case STATELISTEN:
            case STATENONE:
                Toast.makeText(mActivity, "Lost. Listenning", Toast.LENGTH_SHORT).show();
                break;
        }
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
        Log.d("by cho", "start function");
        if(mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if(mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if(mAcceptThread == null) {
            Log.d("by cho", "create accept thread");
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }

        Log.d("by cho", "done start function");
        userFeedback();
    }

    public synchronized void connect(BluetoothDevice device) {
        Log.d("by cho", "connect");
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

        Log.d("by cho", "before create connect thread");
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATECONNECTING);

        userFeedback();
        Log.d("by cho", "done connect function");
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

        if(mAcceptThread!= null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        setState(STATECONNECTED);

        userFeedback();
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

        if(mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        setState(STATENONE);
        userFeedback();
    }

    public void write(byte[] out) {
        ConnectedThread r;
        synchronized(this) {
            if(mState != STATECONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mSocket;

        public AcceptThread() {

            BluetoothServerSocket tmp = null;

            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(mRival, uuid);
            } catch (IOException e) {}

            mSocket = tmp;
            mState = STATELISTEN;
        }

        public void run() {
            Log.d("by cho", "accept thread");
            BluetoothSocket socket = null;

            while(mState != STATECONNECTED) {
                try {
                    socket = mSocket.accept();
                } catch (IOException e) {
                    Log.d("by cho", "accept fail");
                    break;
                }

                if (socket != null) {
                    synchronized(CombatManager.this) {
                        switch(mState) {
                            case STATELISTEN:
                            case STATECONNECTING:
                                Log.d("by cho", "call connected function");
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATENONE:
                            case STATECONNECTED:
                                try {
                                    Log.d("by cho", "close socket");
                                    mSocket.close();
                                } catch (IOException e) {
                                    Log.d("by cho", "exception for close socket");
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {}
        }
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
            Log.d("by cho", "connection thread");

            try {
                mSocket.connect();
            } catch (IOException e) {
                setState(STATELISTEN);

                try {
                    mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                Log.d("by cho", "fail socket connection");
                CombatManager.this.start();
                e.printStackTrace();
                Log.d("by cho", "restart class");

                return;
            }

            synchronized(CombatManager.this) {
                mConnectThread = null;
            }

            Log.d("by cho", "done connection thread");
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
