package com.mpvmedical.sonodrop;

import android.bluetooth.BluetoothDevice;

public class DeviceInfo {

    public BluetoothDevice mDevice;
    public String mDeviceMessage;

    public DeviceInfo(BluetoothDevice device, String message) {
        mDevice = device;
        mDeviceMessage = message;
    }
}