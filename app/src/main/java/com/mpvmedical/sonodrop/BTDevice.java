package com.mpvmedical.sonodrop;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BTDevice extends RecyclerView.Adapter<BTDevice.ViewHolder> {
    private DeviceInfo[] mDataset;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onClick(View view, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final LinearLayout mLinearLayout;

        public ViewHolder(LinearLayout l) {
            super(l);
            mLinearLayout = l;
        }
    }

    public BTDevice(DeviceInfo[] myDataset, OnItemClickListener listener) {
        mDataset = myDataset;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(parent.getContext());
        View v = vi.inflate(R.layout.device_list_item, parent, false);
        LinearLayout lv = (LinearLayout) v.findViewById(R.id.deviceLayout);
        return new ViewHolder(lv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        TextView deviceName = (TextView) holder.mLinearLayout.findViewById(R.id.deviceName);
        deviceName.setText(getAlias(mDataset[position].mDevice));
        holder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onClick(view, position);
            }
        });
    }

    private String getAlias(BluetoothDevice bluetoothDevice) {
        try {
            Method method = bluetoothDevice.getClass().getMethod("getAlias");
            if(method != null) {
                String alias = (String)method.invoke(bluetoothDevice);
                if (alias == null || alias.equals(""))
                    return bluetoothDevice.getName();
                else
                    return alias;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
