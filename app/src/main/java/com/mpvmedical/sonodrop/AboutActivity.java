package com.mpvmedical.sonodrop;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.Stack;

public class AboutActivity extends AppCompatActivity {

    public static Stack<Class<?>> parents = new Stack<>();
    public BluetoothDevice mBluetoothDevice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mBluetoothDevice == null && getIntent().hasExtra("btdevice")) {
            mBluetoothDevice = getIntent().getExtras().getParcelable("btdevice");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent parentActivityIntent = new Intent(this, parents.pop());
                if (mBluetoothDevice != null)
                    parentActivityIntent.putExtra("btdevice", mBluetoothDevice);
                NavUtils.navigateUpTo(this, parentActivityIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
