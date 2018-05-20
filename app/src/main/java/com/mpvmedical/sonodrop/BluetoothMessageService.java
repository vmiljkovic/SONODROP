package com.mpvmedical.sonodrop;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothMessageService {

    // Unique UUID for rn42 device
    private static UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mAdapter;
    BluetoothDevice mBluetoothDevice;
    private final Handler mHandler;
    private final Context mContext;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int failedConnections = 0;

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public BluetoothMessageService(Context context, Handler handler, BluetoothDevice bluetoothDevice) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        mContext = context;
        mBluetoothDevice = bluetoothDevice;
    }

    private synchronized void setState(int state) {
        mState = state;
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void connect() {

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread();
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket) {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, mBluetoothDevice.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    public void write(byte[] out) {
        ConnectedThread r;

        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }

        r.write(out);
    }

    private void connectionFailed() {
        failedConnections++;
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, mContext.getString(R.string.connection_failed));
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        if (failedConnections < 3)
            BluetoothMessageService.this.connect();
    }

    private void connectionLost() {
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, mContext.getString(R.string.connection_lost));
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        //BluetoothMessageService.this.connect();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread() {
            BluetoothSocket tmp = null;

            try {
                tmp = mBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
            } catch (IOException e) {

            }
            mmSocket = tmp;
        }

        public void run() {
            mAdapter.cancelDiscovery();

            try {
                if(!mmSocket.isConnected())
                    mmSocket.connect();
            } catch (IOException e) {

                try {
                    SystemClock.sleep(1000);
                    mmSocket.close();
                    SystemClock.sleep(1000);
                } catch (IOException e2) {

                }
                connectionFailed();
                return;
            }

            synchronized (BluetoothMessageService.this) {
                mConnectThread = null;
            }

            connected(mmSocket);
        }

        public void cancel() {
            try {
                SystemClock.sleep(1000);
                mmSocket.close();
                SystemClock.sleep(1000);
            } catch (IOException e) {

            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
           mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            String message = null;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    if (message == null)
                        message = new String(buffer, 0, bytes);
                    else
                        message += new String(buffer, 0, bytes);

                    if (message.length() > 3 && message.charAt(0) == '%' && message.charAt(message.length() - 1) == ';') {
                        mHandler.obtainMessage(Constants.MESSAGE_READ, -1, -1, message).sendToTarget();
                        message = null;
                    }
                } catch (IOException e) {

                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

            } catch (IOException e) {

            }
        }

        public void cancel() {
            try {
                mmInStream.close();
                mmOutStream.close();
                SystemClock.sleep(1000);
                mmSocket.close();
                SystemClock.sleep(1000);
            } catch (IOException e) {

            }
        }
    }
}
