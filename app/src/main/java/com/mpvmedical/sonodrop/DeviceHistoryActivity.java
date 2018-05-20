package com.mpvmedical.sonodrop;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Stack;

public class DeviceHistoryActivity extends AppCompatActivity {

    String mHistoryDeviceMessage = null;
    public static Stack<Class<?>> parents = new Stack<>();
    public BluetoothDevice mBluetoothDevice = null;

    ArrayList<Integer> finished = new ArrayList<>();
    int finishedMax = 0;
    ArrayList<Integer> errors = new ArrayList<>();
    int errorsMax = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHistoryDeviceMessage = getIntent().getExtras().getString("devicehistory");
        mBluetoothDevice = getIntent().getExtras().getParcelable("btdevice");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout sessions = (LinearLayout) findViewById(R.id.finished);
        LinearLayout errorsTop = (LinearLayout) findViewById(R.id.errors);

        if (mHistoryDeviceMessage != null) {
            if (ParseHistoryMessage(mHistoryDeviceMessage)) {
                SetUI();
                if (finishedMax == 0)
                    sessions.setVisibility(View.INVISIBLE);

                if (errorsMax == 0)
                    errorsTop.setVisibility(View.INVISIBLE);
            }
            else {
                sessions.setVisibility(View.INVISIBLE);
                errorsTop.setVisibility(View.INVISIBLE);
            }
        }
    }

    private boolean ParseHistoryMessage(String readMessage) {
        char eeprom = readMessage.charAt(4);
        if (Character.getNumericValue(eeprom) > 0) {
            getSupportActionBar().setSubtitle(getString(R.string.memory_error));
            return false;
        }

        int start = 5;
        int minute = 5;
        int sum = 0;
        for (int i = 0; i < 20; i++) {
            String word = readMessage.substring(start, start + 4);
            if (word.equals("FFFF"))
                finished.add(0);
            else {
                int value = Integer.parseInt(word, 16);
                sum += value * minute;
                finished.add(value);
            }
            start = start + 4;
            minute += 5;
        }

        for (int i = 0; i < 10; i++) {
            String word = readMessage.substring(start, start + 4);
            if (word.equals("FFFF"))
                errors.add(0);
            else
                errors.add(Integer.parseInt(word, 16));
            start = start + 4;
        }

        TextView totalSum = (TextView) findViewById(R.id.device_history_info);
        totalSum.setText(String.format(getString(R.string.total_working_time), sum));

        finishedMax = setMax(finished);
        errorsMax = setMax(errors);

        return true;
    }

    private int setMax(ArrayList<Integer> list) {
        int max = list.get(0);

        for(Integer i: list) {
            if(i > max) max = i;
        }

        return max;
    }

    private void SetUI() {
        LinearLayout finishedLayout = (LinearLayout) findViewById(R.id.finished);
        for (int i = 0; i < 20; i++) {
            LinearLayout ll = (LinearLayout) finishedLayout.getChildAt(i);
            for (int j = 0; j < 2; j++) {
                View view = ll.getChildAt(j);
                if (view instanceof VerticalProgressBar) {
                    VerticalProgressBar vs = (VerticalProgressBar) view;
                    float div = (float)vs.getLayoutParams().height / finishedMax;
                    vs.getLayoutParams().height = (int) (finished.get(i) * div);
                    if (finished.get(i) == 0) continue;
                    LinearLayout parent = (LinearLayout) vs.getParent();
                    final TextView verticalText = (TextView) parent.getChildAt(0);
                    verticalText.setText(finished.get(i) + "");
                }
            }
        }

        LinearLayout errorsLayout = (LinearLayout) findViewById(R.id.errors);
        for (int i = 0; i < 10; i++) {
            LinearLayout ll = (LinearLayout) errorsLayout.getChildAt(i);
            for (int j = 0; j < 2; j++) {
                View view = ll.getChildAt(j);
                if (view instanceof VerticalProgressBar) {
                    VerticalProgressBar vs = (VerticalProgressBar) view;
                    float div = (float)vs.getLayoutParams().height / errorsMax;
                    vs.getLayoutParams().height = (int)(errors.get(i) * div);
                    if (errors.get(i) == 0) continue;
                    LinearLayout parent = (LinearLayout) vs.getParent();
                    final TextView verticalText = (TextView) parent.getChildAt(0);
                    verticalText.setText(errors.get(i) + "");
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mBluetoothDevice != null) {
            CharSequence generic = getSupportActionBar().getTitle();
            getSupportActionBar().setTitle(getAlias(mBluetoothDevice) + "  " + generic);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent parentActivityIntent = new Intent(this, parents.pop());
                if (null != parentActivityIntent) {
                    parentActivityIntent.putExtra("btdevice", mBluetoothDevice);
                }
                NavUtils.navigateUpTo(this, parentActivityIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
}
