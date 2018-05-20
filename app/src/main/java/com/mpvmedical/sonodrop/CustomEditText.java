package com.mpvmedical.sonodrop;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import static com.mpvmedical.sonodrop.R.id.delayHour;
import static com.mpvmedical.sonodrop.R.id.delayMinut;

public class CustomEditText extends android.support.v7.widget.AppCompatEditText {

    Context _Context;

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        _Context = context;
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        _Context = context;
    }

    public CustomEditText(Context context) {
        super(context);
        _Context = context;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            InputMethodManager mgr = (InputMethodManager) _Context.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (_Context instanceof MainActivitySonic) {
                MainActivitySonic main = (MainActivitySonic) _Context;

                if (!CheckTime(this)) {
                    mgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
                    return true;
                }

                dispatchKeyEvent(event);
                if (main.isPaused) {
                    main.mStatusHandler.postDelayed(main.runnable, main.DELAY);
                    main.isPaused = !main.isPaused;
                }
            }

            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    private boolean CheckTime(TextView v) {
        String str = v.getText().toString();
        int value;
        try {
            value = Integer.parseInt(str);
        } catch(NumberFormatException nfe) {
            return false;
        }

        if (v.getId() == delayHour) {
            if (value >= 24)
                return false;
        } else if (v.getId() == delayMinut) {
            if (value >= 60)
                return false;
        } else {
            if (value < 1 || value > 99)
                return false;
        }

        return true;
    }

    private boolean CheckFullTime(TextView v) {
        String str = v.getText().toString();
        if (str.length() != 2) {
            return false;
        }

        char c0 = str.charAt(0);
        char c1 = str.charAt(1);
        return (c0 >= '1' && c0 <= '9') && (c1 >= '0' && c1 <= '9');
    }
}