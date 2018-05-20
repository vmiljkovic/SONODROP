package com.mpvmedical.sonodrop;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class WorkingSettingsSonic implements Serializable {
    public int fan;
    public int aerosol;
    public int heater;
    int duration;

    public WorkingSettingsSonic() {
        fan = 0;
        duration = 0;
        aerosol = 0;
        heater = 0;
    }

    public String toString() {
        String aerosolText = Integer.toHexString(aerosol);
        String heaterText = Integer.toHexString(heater);
        String fanText = Integer.toHexString(fan + 1);

        return aerosolText + heaterText + fanText + duration;
    }
}