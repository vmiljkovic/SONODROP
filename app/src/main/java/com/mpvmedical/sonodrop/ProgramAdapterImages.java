package com.mpvmedical.sonodrop;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Method;

public class ProgramAdapterImages extends ArrayAdapter<Drawable>{

    private Context context;
    private Spinner mSpinner;
    static private Drawable[] Dosage;

    public ProgramAdapterImages(Context context, int textViewResourceId, Drawable[] objects, Spinner spinner) {
        super(context, textViewResourceId, objects);
        this.context = context;
        if (Dosage == null)
            Dosage = objects;
        mSpinner = spinner;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView)convertView;

        if (convertView == null) {
            convertView = new TextView(context);
            label = (TextView)convertView;
        }

        label.setBackground(Dosage[position]);

        return(convertView);
    }

    public View getCustomView(final int position, ViewGroup parent) {
        ImageView imageView = new ImageView(getContext());
        imageView.setBackground(Dosage[position]);
        imageView.setTag(position);
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    int pos = (int)v.getTag();
                    mSpinner.setSelection(pos);
                    MainActivitySonic main = (MainActivitySonic)context;
                    main.programSecondary = false;
                    //main.isSmallDose = pos == 1;
                    main.getUIWorkingSettings();
                    main.mMessageQueue.add(Constants.SET_CONTROLER_SETTINGS + main.mWorkingSettings.toString() + ";");
                    Method method = Spinner.class.getDeclaredMethod("onDetachedFromWindow");
                    method.setAccessible(true);
                    method.invoke(mSpinner);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return imageView;
    }
}