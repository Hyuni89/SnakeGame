package com.example.cho.snake.connection;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.GridLayout;

import com.example.cho.snake.MainActivity;
import com.example.cho.snake.SnakeEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
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
    private SnakeEngine engine;
    private GridLayout mRivalGridMap;

    static final public int REQUEST_ENABLE_BT = 100;
    static final public int REQUEST_CONNECT_DEVICE = 101;

    static final public int SHOW_TOAST = 150;
    static final public int STATENONE = 200;
    static final public int STATELISTEN = 201;
    static final public int STATECONNECTING = 202;
    static final public int STATECONNECTED = 203;

//    static final private UUID uuid = UUID.fromString("00000000-0000-1000-8000-00805F9B34FB");
    private static final UUID uuid = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
//    private static final UUID uuid= UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    static final private String mRival = "Rival";

    static final public int BUFSIZE = 1024;

    public CombatManager(Activity activity, Handler handler, SnakeEngine engine) {
        mHandler = handler;
        mActivity = activity;
        mRivalGridMap= null;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.engine = engine;
        setState(STATENONE);
    }

    private boolean getDeviceState() {
        return (mBluetoothAdapter != null);
    }

    private void userFeedback() {
        Message msg = new Message();
        switch(mState) {
            case STATECONNECTED:
                msg.what = STATECONNECTED;
                break;
            case STATECONNECTING:
                msg.what = STATECONNECTING;
                break;
            case STATELISTEN:
            case STATENONE:
                msg.what = STATELISTEN;
                break;
        }
        mHandler.sendMessage(msg);
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
        if(mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        if(mState != STATECONNECTED) ((MainActivity)mActivity).showDevicesListState();
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

        if(mAcceptThread == null && mBluetoothAdapter.isEnabled()) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }

        mRivalGridMap = null;
        userFeedback();
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

        if(mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(socket, engine);
        mConnectedThread.start();
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
            } catch (IOException e) {
                e.printStackTrace();
            }

            mSocket = tmp;
            mState = STATELISTEN;
        }

        public void run() {
            BluetoothSocket socket = null;

            while(mState != STATECONNECTED) {
                try {
                    socket = mSocket.accept();
                } catch (IOException e) {
                    break;
                }

                if (socket != null) {
                    synchronized(CombatManager.this) {
                        switch(mState) {
                            case STATELISTEN:
                            case STATECONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATENONE:
                            case STATECONNECTED:
                                try {
                                    mSocket.close();
                                } catch (IOException e) {
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

            setState(STATECONNECTING);
            userFeedback();
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
        private final SnakeEngine engine;
        private int[][] rivalMap;
        private int size;

        public ConnectedThread(BluetoothSocket socket, SnakeEngine engine) {
            mSocket = socket;
            InputStream tmpInput = null;
            OutputStream tmpOutput = null;
            this.engine = engine;
            size = 0;

            try {
                tmpInput = mSocket.getInputStream();
                tmpOutput = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mInput = tmpInput;
            mOutput = tmpOutput;

            setState(STATECONNECTED);
            userFeedback();
        }

        public void run() {
            byte[] buffer = new byte[BUFSIZE];
            int bytes = 0;

            while(mState == STATECONNECTED) {
                try {
                    bytes = mInput.read(buffer);
                } catch (IOException e) {
                    setState(STATELISTEN);
                    userFeedback();
                    e.printStackTrace();
                    break;
                }
                if(buffer[0] == 0) continue;
                if(mRivalGridMap != null) {
                    decode(buffer);
                    ((MainActivity)mActivity).show(mRivalGridMap, rivalMap);
                }
            }

            setState(STATELISTEN);
            CombatManager.this.start();
        }

        public void write(byte[] buffer) {
            encode(buffer);

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

        public void encode(byte[] buffer) {
            Arrays.fill(buffer, (byte)0);
            int map[][] = engine.getMap();
            int n = map.length - 2;
            int cnt = 0;
            buffer[cnt++] = (byte)n;

            for(int i = 1; i <= n; i++) {
                for(int j = 1; j <= n; j++) {
                    buffer[cnt++] = (byte)map[i][j];
                }
            }
        }

        public void decode(byte[] buffer) {
            int n = buffer[0];
            if(size != n) {
                size = n;
                rivalMap = new int[n + 2][n + 2];
                for(int i = 0; i < n + 2; i++) {
                    Arrays.fill(rivalMap[i], -1);
                }
                ((MainActivity)mActivity).initMap(mRivalGridMap, size);
            }

            for(int i = 0; i < n; i++) {
                for(int j = 0; j < n; j++) {
                    rivalMap[i + 1][j + 1] = buffer[i * n + j + 1];
                }
            }
        }
    }

    public void setRivalMap(GridLayout map) {
        mRivalGridMap = map;
    }
}
