package com.mpvmedical.sonodrop;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Set;

public class WorkingLayout extends Fragment implements BTDevice.OnItemClickListener {

    private static final int REQUEST_ENABLE_BT = 3;
    private BluetoothAdapter mBluetoothAdapter = null;
    DeviceInfo[] mDeviceList = null;

    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            mDeviceList = getPairedDevices();
            mRecyclerView.setAdapter(new BTDevice(mDeviceList, this));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_working_layout, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.paired_devices);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onClick(View view, int position) {
        selectItem(position);
    }

    private void selectItem(int position) {
        mBluetoothAdapter.cancelDiscovery();
        String deviceName = mDeviceList[position].mDevice.getName();
        Intent intent = null;
        intent = new Intent(getActivity(), MainActivitySonic.class);
        intent.putExtra("btdevice", mDeviceList[position].mDevice);
        startActivity(intent);
    }

    public DeviceInfo[] getPairedDevices() {
        Set<BluetoothDevice> paired = mBluetoothAdapter.getBondedDevices();

        DeviceInfo[] result = null;
        if (paired.size() == 0) {
            Toast.makeText(getActivity(), "There are no paired bluetooth devices", Toast.LENGTH_LONG).show();
        } else {
            result = new DeviceInfo[paired.size()];
            int i = 0;
            for (BluetoothDevice device : paired) {
                if (device.getName().startsWith("PSN_IF-") || device.getName().startsWith("PSONIC_IF-")) {
                    result[i] = new DeviceInfo(device, "");
                    i++;
                }
            }
        }

        return clean(result);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    mDeviceList = getPairedDevices();
                    mRecyclerView.setAdapter(new BTDevice(mDeviceList, this));
                } else {
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    public DeviceInfo[] clean(final DeviceInfo[] v) {
        int r, w;
        final int n = r = w = v.length;
        while (r > 0) {
            final DeviceInfo s = v[--r];
            if (s != null) {
                v[--w] = s;
            }
        }
        return Arrays.copyOfRange(v, w, n);
    }
}
