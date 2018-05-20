package com.mpvmedical.sonodrop;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class WorkingSettings implements Serializable {
    public int fan;
    public int[] doser;
    int duration;

    public WorkingSettings() {
        fan = 0;
        duration = 0;
        doser = new int[10];
    }

    public static boolean saveWorkingSettings(Context context, WorkingSettings workingSettings, String fileName) {
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(workingSettings);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static WorkingSettings getWorkingSettings(Context context, String fileName) {
        try {
            FileInputStream fis = context.openFileInput(fileName);
            ObjectInputStream is = new ObjectInputStream(fis);
            Object readObject = is.readObject();
            is.close();
            fis.close();

            if(readObject != null && readObject instanceof WorkingSettings) {
                return (WorkingSettings) readObject;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String toString() {
        StringBuilder dosers = new StringBuilder();
        for (int i : doser) {
            dosers.append(Integer.toHexString(i));
        }
        String text = dosers.toString();
        String fanText = Integer.toHexString(fan + 1);

        return text + fanText + duration;
    }
}