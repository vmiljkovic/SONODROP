package com.mpvmedical.sonodrop;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivitySonic extends AppCompatActivity implements ChangeAliasDialog.NoticeChangeAliasDialogListener{

    public BluetoothDevice mBluetoothDevice = null;

    private BluetoothMessageService mChatService = null;
    public Handler mStatusHandler = new Handler();
    public Queue<String> mMessageQueue = new LinkedList<>();
    private String mLastSentMessage = null;
    WorkingSettingsSonic mWorkingSettings = new WorkingSettingsSonic();
    boolean mUpdatePrimary = false;
    private int mStatus = -1;
    private int mError = 0;
    //private int mDevice = 1;
    private boolean mEnableAll = false;
    private String mHistoryDeviceMessage = null;
    private int mDelayHours = 0;
    private int mDelayMinutes = 0;

    TextView estimatedTime = null;
    CustomEditText fullTime = null;
    //Spinner programSpinner = null;
    ToggleButton stopBtn = null;
    ToggleButton pauseBtn = null;
    SeekBar fan = null;
    SeekBar heater = null;
    SeekBar aerosol = null;
    ImageView imageView = null;
    ProgressBar progressBar = null;
    ActionBar mActionBar = null;
    TextView mTitleText = null;
    TextView mSubTitleText = null;
    CustomEditText txtDelayHour = null;
    CustomEditText txtDelayMinut = null;
    RelativeLayout heaterLayout = null;
    ImageView imageHeater = null;
    GridLayout gridLayout = null;
    TextView heaterText = null;

    MenuItem actionHistory = null;

    boolean isSmallDose = false;
    boolean isPaused = false;
    boolean isDelayStart = false;
    boolean isHeaterConnected = false;
    //boolean isReadOnly = false;
    boolean programSecondary = false;
    int DELAY = 300; // ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sonic);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_sonic);
        setSupportActionBar(toolbar);

        mActionBar = getSupportActionBar();

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.action_bar_custom, null);
        mTitleText = (TextView) mCustomView.findViewById(R.id.title_text);
        mSubTitleText = (TextView) mCustomView.findViewById(R.id.subtitle_text);

        ImageButton imageButton = (ImageButton) mCustomView.findViewById(R.id.imageButton);
        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP){
            imageButton.setVisibility(View.INVISIBLE);
        }
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ChangeAliasDialog dialog = new ChangeAliasDialog();
                dialog.mAlias = getAlias(mBluetoothDevice);
                dialog.show(getSupportFragmentManager(), "ChangeAliasDialog");
            }
        });

        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        InitUI();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mBluetoothDevice == null && getIntent().hasExtra("btdevice")) {
            mBluetoothDevice = getIntent().getExtras().getParcelable("btdevice");
        }

        if (mBluetoothDevice != null) {
            mTitleText.setText(getAlias(mBluetoothDevice));
            mActionBar.setDisplayShowCustomEnabled(true);

            if (mChatService == null)
                mChatService = new BluetoothMessageService(this, mHandler, mBluetoothDevice);
        }

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mStatusHandler.removeCallbacks(runnable);

        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mChatService != null) {
            if (mChatService.getState() == BluetoothMessageService.STATE_NONE) {
                mChatService.connect();
            }
        }
    }

    private void InitUI() {
        final TextView fanText = (TextView) findViewById(R.id.fantextid);
        fan = (SeekBar) findViewById(R.id.fan);
        fanText.setText("1");
        fan.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = Integer.toString(progress + 1);
                fanText.setText(text);
                if (fromUser) {
                    getUIWorkingSettings();
                    mMessageQueue.add(Constants.SET_CONTROLER_SETTINGS + mWorkingSettings.toString() + ";");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        stopBtn = (ToggleButton) findViewById(R.id.stopBtn);
        pauseBtn = (ToggleButton) findViewById(R.id.pauseBtn);

        gridLayout = (GridLayout) findViewById(R.id.gridLayout);

        estimatedTime = (TextView) findViewById(R.id.estimatedTime);
        fullTime = (CustomEditText) findViewById(R.id.fullTime);
        fullTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    if (!isPaused) {
                        mStatusHandler.removeCallbacks(runnable);
                        isPaused = !isPaused;
                    }

                    // Execute some code after 2 seconds have passed
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            fullTime.setText("");
                        }
                    }, DELAY);
                }
                return false;
            }
        });

        fullTime.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            if (!CheckTime(v)) {
                                Toast.makeText(getApplicationContext(), R.string.durationmessage, Toast.LENGTH_SHORT).show();
                                return true;
                            }

                            if (!mUpdatePrimary) {
                                getUIWorkingSettings();
                                mMessageQueue.add(Constants.SET_CONTROLER_SETTINGS + mWorkingSettings.toString() + ";");
                            }
                        }

                        if (isPaused) {
                            mStatusHandler.postDelayed(runnable, DELAY);
                            isPaused = !isPaused;
                        }

                        return false;
                    }
                });

        txtDelayHour = (CustomEditText) findViewById(R.id.delayHour);
        txtDelayMinut = (CustomEditText) findViewById(R.id.delayMinut);
        txtDelayHour.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    if (!isPaused) {
                        mStatusHandler.removeCallbacks(runnable);
                        isPaused = !isPaused;
                    }

                    // Execute some code after 2 seconds have passed
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            txtDelayHour.setText("");
                        }
                    }, DELAY);
                }
                return false;
            }
        });

        txtDelayHour.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            if (!CheckTime(v)) {
                                //Toast.makeText(getApplicationContext(), R.string.durationmessage, Toast.LENGTH_SHORT).show();
                                return true;
                            }

                            int hours, minutes;
                            Date dt = new Date();
                            hours = dt.getHours();
                            minutes = dt.getMinutes();
                            int currentTime = hours * 60 + minutes;
                            if ((mDelayHours * 60 + mDelayMinutes) != currentTime) {
                                isDelayStart = true;
                                v.setTextColor(getResources().getColor(R.color.colorPrimary));
                            }
                        }

                        if (isPaused) {
                            mStatusHandler.postDelayed(runnable, DELAY);
                            isPaused = !isPaused;
                        }

                        return false;
                    }
                });

        txtDelayMinut.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    if (!isPaused) {
                        mStatusHandler.removeCallbacks(runnable);
                        isPaused = !isPaused;
                    }

                    // Execute some code after 2 seconds have passed
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            txtDelayMinut.setText("");
                        }
                    }, DELAY);
                }
                return false;
            }
        });

        txtDelayMinut.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            if (!CheckTime(v)) {
                                //Toast.makeText(getApplicationContext(), R.string.durationmessage, Toast.LENGTH_SHORT).show();
                                return true;
                            }

                            int hours, minutes;
                            Date dt = new Date();
                            hours = dt.getHours();
                            minutes = dt.getMinutes();
                            int currentTime = hours * 60 + minutes;
                            if ((mDelayHours * 60 + mDelayMinutes) != currentTime) {
                                isDelayStart = true;
                                v.setTextColor(getResources().getColor(R.color.colorPrimary));
                            }
                        }

                        if (isPaused) {
                            mStatusHandler.postDelayed(runnable, DELAY);
                            isPaused = !isPaused;
                        }

                        return false;
                    }
                });

//        programSpinner = (Spinner)findViewById(R.id.program);
//        programSpinner.setAdapter(new ProgramAdapterImages(this, R.layout.row_image, new Drawable[] {getResources().getDrawable(R.drawable.profisonic_aerosol_large),
//                getResources().getDrawable(R.drawable.profisonic_aerosol_small)}, programSpinner));
//        programSpinner.setSelection(0);

        heaterLayout = (RelativeLayout) findViewById(R.id.heaterLayout);
        imageHeater = (ImageView) findViewById(R.id.imageHeater);
        heaterText = (TextView) findViewById(R.id.heatertextid);
        heater = (SeekBar) findViewById(R.id.heater);
        heaterText.setText("0");
        heater.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = Integer.toString(progress);
                heaterText.setText(text);
                if (fromUser) {
                    getUIWorkingSettings();
                    mMessageQueue.add(Constants.SET_CONTROLER_SETTINGS + mWorkingSettings.toString() + ";");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final TextView aerosolText = (TextView) findViewById(R.id.aerosoltextid);
        aerosol = (SeekBar) findViewById(R.id.aerosol);
        aerosolText.setText("1");
        aerosol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String text = Integer.toString(progress + 1);
                aerosolText.setText(text);
                if (fromUser) {
                    getUIWorkingSettings();
                    mMessageQueue.add(Constants.SET_CONTROLER_SETTINGS + mWorkingSettings.toString() + ";");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imageView = (ImageView) findViewById(R.id.imageView);

        setEnabledAll(mEnableAll = false);
    }

    private boolean CheckTime(TextView v) {
        String str = v.getText().toString();
        int value;
        try {
            value = Integer.parseInt(str);
        } catch(NumberFormatException nfe) {
            return false;
        }

        if (v.getId() == R.id.delayHour) {
            if (value >= 24)
                return false;
            mDelayHours = value;
        } else if (v.getId() == R.id.delayMinut) {
            if (value >= 60)
                return false;
            mDelayMinutes = value;
        } else {
            if (value < 1 || value > 99)
                return false;
        }
        return true;
    }

    public void setEnabledAll(boolean enabled) {
        if (enabled) {
            fan.setVisibility(View.VISIBLE);
            heater.setVisibility(View.VISIBLE);
            aerosol.setVisibility(View.VISIBLE);
        }
        else {
            fan.setVisibility(View.INVISIBLE);
            heater.setVisibility(View.INVISIBLE);
            aerosol.setVisibility(View.INVISIBLE);
        }

        if (!isHeaterConnected) {
            heater.setVisibility(View.INVISIBLE);
            heaterText.setVisibility(View.INVISIBLE);
        } else {
            heater.setVisibility(View.VISIBLE);
            heaterText.setVisibility(View.VISIBLE);
        }

        //programSpinner.setEnabled(enabled);
        txtDelayMinut.setEnabled(enabled);
        txtDelayHour.setEnabled(enabled);
        fullTime.setEnabled(enabled);
        pauseBtn.setEnabled(enabled);
        stopBtn.setEnabled(enabled);
        if (actionHistory != null)
            actionHistory.setEnabled(enabled);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_sonic, menu);
        actionHistory = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            AboutActivity.parents.push(getClass());
            Intent intent = new Intent(this, AboutActivity.class);
            intent.putExtra("btdevice", mBluetoothDevice);
            startActivity(intent);
        }
        else if (id == R.id.action_device_history) {
            mMessageQueue.add(Constants.GET_CONTROLER_HISTORY);
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendMessage(String message) {
       if (mChatService.getState() != BluetoothMessageService.STATE_CONNECTED) {
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            //mStartTime = System.currentTimeMillis();
            mChatService.write(send);
        }
    }

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String nextMessage = mMessageQueue.poll();
            if (nextMessage == null) {
                mLastSentMessage = Constants.GET_CONTROLER_STATUS;
                sendMessage(Constants.GET_CONTROLER_STATUS);
            } else if (mLastSentMessage != null && !mLastSentMessage.equals(Constants.GET_CONTROLER_STATUS)) {
                mLastSentMessage = Constants.GET_CONTROLER_STATUS;
                sendMessage(Constants.GET_CONTROLER_STATUS);
            } else {
                Iterator it = mMessageQueue.iterator();
                while (it.hasNext()) {
                    String message = (String) it.next();
                    if (nextMessage.regionMatches(0, message, 0, 3)) {
                        nextMessage = message;
                        it.remove();
                    }
                }

                mLastSentMessage = nextMessage;
                sendMessage(nextMessage);
            }

            mStatusHandler.postDelayed(this, DELAY);
        }
    };

    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothMessageService.STATE_CONNECTED:
                            mSubTitleText.setText(R.string.connected);
                            mMessageQueue.add(Constants.GET_CONTROLER_STATUS);
                            mUpdatePrimary = true;
                            mStatusHandler.postDelayed(runnable, 0);
                            break;
                        case BluetoothMessageService.STATE_CONNECTING:
                            mSubTitleText.setText(R.string.connecting);
                            break;
                        case BluetoothMessageService.STATE_NONE:
                            mSubTitleText.setText(R.string.not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    DispatchMessage(readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    String mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(MainActivitySonic.this, getString(R.string.connected_to) + mConnectedDeviceName, Toast.LENGTH_LONG).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    String toastMessage = msg.getData().getString(Constants.TOAST);
                    if (toastMessage.equals(getString(R.string.connection_failed))) {
                        setEnabledAll(mEnableAll = false);
                        mSubTitleText.setText(getString(R.string.connection_failed));
                    }
                    Toast.makeText(MainActivitySonic.this, msg.getData().getString(Constants.TOAST), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private void DispatchMessage(String readMessage) {
        if (readMessage.regionMatches(0, Constants.GET_CONTROLER_STATUS, 0, 3)) {
            mWorkingSettings = ParseStatusMessage(readMessage);
            setEnabledAll(mEnableAll);
            if (mEnableAll) {
                setUIForStatus();
                setUIWorkingSettings(false);
                setUIWorkingSettings(true);
            }
            mUpdatePrimary = false;
        } else if (readMessage.regionMatches(0, Constants.GET_CONTROLER_HISTORY, 0, 3)) {
            mHistoryDeviceMessage = readMessage;
            DeviceHistoryActivity.parents.push(getClass());
            Intent intent = new Intent(this, DeviceHistoryActivity.class);
            intent.putExtra("devicehistory", mHistoryDeviceMessage);
            intent.putExtra("btdevice", mBluetoothDevice);
            startActivity(intent);
        }
    }

    private WorkingSettingsSonic ParseStatusMessage(String readMessage) {
        WorkingSettingsSonic workingSettings = new WorkingSettingsSonic();
        //char device = readMessage.charAt(4);
        //mDevice = Character.getNumericValue(device);
        char heaterStatus = readMessage.charAt(5);
        char status = readMessage.charAt(6);
        char error = readMessage.charAt(7);
        CharSequence duration = readMessage.subSequence(8, 10);
        int newDuration = Integer.parseInt(duration.toString());
        if (newDuration != workingSettings.duration)
            workingSettings.duration = newDuration;
        CharSequence left = readMessage.subSequence(10, 14);
        int time = Integer.parseInt(left.toString());
        char aerosol = readMessage.charAt(14);
        workingSettings.aerosol = Character.getNumericValue(aerosol);
        isSmallDose = workingSettings.aerosol <= 7;
        char heater = readMessage.charAt(15);
        workingSettings.heater = Character.getNumericValue(heater);
        char fan = readMessage.charAt(16);
        workingSettings.fan = Character.getNumericValue(fan);
        workingSettings.fan--;

        CharSequence delayText = readMessage.subSequence(17, 21);
        int delay = Integer.parseInt(delayText.toString());

        SetUIOtherSettings(Character.getNumericValue(heaterStatus), Character.getNumericValue(status), Character.getNumericValue(error), time, newDuration, delay);

        return workingSettings;
    }

    private void SetUIOtherSettings(int heaterStatus, int status, int error, int time, int duration, int delayTime) {
        programSecondary = true;

        mError = error;
        mEnableAll = true;

        if (heaterStatus >= 8) {
            // heater exists
            int heaterConnection = heaterStatus - 8;
            if (heaterConnection >= 2) {
                // heater connected
                isHeaterConnected = true;
                imageHeater.setBackground(getResources().getDrawable(R.drawable.profisonic_heater));
            } else {
                // heater not connected
                isHeaterConnected = false;
                imageHeater.setBackground(getResources().getDrawable(R.drawable.profisonic_heater_notconn));
            }
        } else {
            // heater does not exists
            isHeaterConnected = false;
            heaterLayout.setVisibility(View.INVISIBLE);
        }

        if (status >= 8) {
            // update from local device
            mStatus = status - 8;
            mUpdatePrimary = true;
            //isReadOnly = true;
        } else {
            // update from here
            mStatus = status;
            //isReadOnly = false;
        }

        if (isSmallDose) {
            gridLayout.setVisibility(View.INVISIBLE);
        } else {
            gridLayout.setVisibility(View.VISIBLE);
        }

        int minutes = (int)Math.floor(time / 60);
        int seconds = time - minutes * 60;
        if (mStatus == 0) {
            String infoTime = getString(R.string.current_time_text) + String.format("%02d:%02d", duration, 0);
            estimatedTime.setText(infoTime);
        } else if (time > 0) {
            String infoTime = getString(R.string.current_time_text) + String.format("%02d:%02d", minutes, seconds);
            estimatedTime.setText(infoTime);
        }

        if (!isDelayStart) {
            Date dt = new Date();
            mDelayHours = dt.getHours();
            mDelayMinutes = dt.getMinutes();
        }

        txtDelayHour.setTextColor(getResources().getColor(R.color.colorPrimary));
        txtDelayMinut.setTextColor(getResources().getColor(R.color.colorPrimary));

        if (delayTime > 0 || mStatus == 3) {
            mDelayHours = (int) Math.floor(delayTime / 60);
            mDelayMinutes = delayTime - mDelayHours * 60;

            txtDelayHour.setTextColor(getResources().getColor(R.color.colorSecondary));
            txtDelayMinut.setTextColor(getResources().getColor(R.color.colorSecondary));
        }

        txtDelayHour.setText(String.format("%02d", mDelayHours));
        txtDelayMinut.setText(String.format("%02d", mDelayMinutes));

        int estimated = duration * 60;
        int played = estimated - time;
        float div = (float) played / estimated;
        progressBar.setProgress((int) (100 * div));
    }

    private void setUIForStatus() {
        String subTitle = getString(R.string.status_text);
        switch (mStatus) {
            case 0: // STOP
                imageView.setBackground(getResources().getDrawable(R.drawable.profisonic_stop));
                pauseBtn.setBackground(getResources().getDrawable(R.drawable.toggleplay));
                pauseBtn.setChecked(true);
                stopBtn.setChecked(false);
                stopBtn.setEnabled(false);
                subTitle += getString(R.string.status_stop);
                break;
            case 1: // PLAY
            case 3:
                // disable fullTime and program
                fullTime.setEnabled(false);
                //programSpinner.setEnabled(false);
                txtDelayHour.setEnabled(false);
                txtDelayMinut.setEnabled(false);
                imageView.setBackground(getResources().getDrawable(R.drawable.profisonic_run));
                if (mStatus == 1) {
                    pauseBtn.setBackground(getResources().getDrawable(R.drawable.togglepause));
                    pauseBtn.setChecked(true);
                    subTitle += getString(R.string.status_play);
                } else {
                    pauseBtn.setChecked(false);
                    pauseBtn.setEnabled(false);
                    subTitle += getString(R.string.status_delay);
                }
                stopBtn.setChecked(true);
                stopBtn.setEnabled(true);
                break;
            case 2: // PAUSE
                // disable fullTime and program
                fullTime.setEnabled(false);
                //programSpinner.setEnabled(false);
                imageView.setBackground(getResources().getDrawable(R.drawable.profisonic_pause));
                pauseBtn.setBackground(getResources().getDrawable(R.drawable.toggleplay));
                pauseBtn.setChecked(true);
                stopBtn.setChecked(true);
                stopBtn.setEnabled(true);
                subTitle += getString(R.string.status_pause);
                break;
        }

        if (mError > 0 ) {
            switch (mError) {
                case 1:
                    imageView.setBackground(getResources().getDrawable(R.drawable.profisonic_stop_min));
                    subTitle += getString(R.string.error_code_21);
                    break;
                case 2:
                    imageView.setBackground(getResources().getDrawable(R.drawable.profisonic_stop_open));
                    subTitle = getString(R.string.error_code) + mError + getString(R.string.error_code_22);
                    break;
                case 4:
                    imageView.setBackground(getResources().getDrawable(R.drawable.profisonic_stop_overheat));
                    subTitle = getString(R.string.error_code) + mError + getString(R.string.error_code_24);
                    break;
                default:
                    imageView.setBackground(getResources().getDrawable(R.drawable.profisonic_normal));
                    subTitle = getString(R.string.error_code) + mError;
                    break;
            }
        }

//        if (isReadOnly)
//            subTitle += getString(R.string.local_mode);

        mSubTitleText.setText(subTitle);
    }

    public void getUIWorkingSettings() {
        mWorkingSettings.fan = fan.getProgress();

        String time = fullTime.getText().toString();
        mWorkingSettings.duration = Integer.parseInt(time);
        mWorkingSettings.heater = heater.getProgress();
        //if (programSpinner.getSelectedItemPosition() == 1)
        //    mWorkingSettings.aerosol = aerosol.getProgress();
        //else
        mWorkingSettings.aerosol = aerosol.getProgress() + 8;
    }

    private void setUIWorkingSettings(Boolean secondary) {
        if (mWorkingSettings == null) return;

        if (!secondary)
            fan.setProgress(mWorkingSettings.fan);
        else
            fan.setSecondaryProgress(mWorkingSettings.fan);

        if (!secondary)
            heater.setProgress(mWorkingSettings.heater);
        else
            heater.setSecondaryProgress(mWorkingSettings.heater);

        //programSpinner.setSelection(isSmallDose ? 1 : 0);

        if (!secondary) {
            if (isSmallDose)
                aerosol.setProgress(mWorkingSettings.aerosol);
            else
                aerosol.setProgress(mWorkingSettings.aerosol - 8);
        } else {
            if (isSmallDose)
                aerosol.setSecondaryProgress(mWorkingSettings.aerosol);
            else
                aerosol.setSecondaryProgress(mWorkingSettings.aerosol - 8);
        }

        fullTime.setText(String.format("%02d", mWorkingSettings.duration));
        if (!secondary)
            fullTime.setTextColor(getResources().getColor(R.color.colorPrimary));
        else
            fullTime.setTextColor(getResources().getColor(R.color.colorSecondary));
    }

    @Override
    public void onChangeAliasDialogPositiveClick(DialogFragment dialog) {
        ChangeAliasDialog changeAliasDialog = (ChangeAliasDialog)dialog;
        try {
            Method method = mBluetoothDevice.getClass().getMethod("setAlias", String.class);
            if(method != null) {
                method.invoke(mBluetoothDevice, changeAliasDialog.mAlias.toString());
                mTitleText.setText(getAlias(mBluetoothDevice));
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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

    public void pauseBtnOnClick(View view) {
        if (mStatus == 0) {
            if (CheckTime(fullTime))
                RunDevice();
            else {
                if (isPaused) {
                    mStatusHandler.postDelayed(runnable, DELAY);
                    isPaused = !isPaused;
                }

                //fullTime.setText("10");
                getUIWorkingSettings();
                mMessageQueue.add(Constants.SET_CONTROLER_SETTINGS + mWorkingSettings.toString() + ";");
                RunDevice();
            }
        }
        else if (mStatus == 1)
            mMessageQueue.add(Constants.SET_CONTROLER_STATUS + "2;");
        else if (mStatus == 2)
            RunDevice();
    }

    public void stopBtnOnClick(View view) {
        mMessageQueue.add(Constants.SET_CONTROLER_STATUS + "0;");
    }

    private void RunDevice() {
        int hours, minutes;
        Date dt = new Date();
        hours = dt.getHours();
        minutes = dt.getMinutes();
        int currentTime = hours * 60 + minutes;
        int delay = mDelayHours * 60 + mDelayMinutes;
        if (isDelayStart && (delay != currentTime)) {
            mMessageQueue.add(Constants.SET_CONTROLER_STATUS + "3" + String.format("%04d", currentTime) + String.format("%04d", delay) + ";");
            isDelayStart = false;
        } else {
            mMessageQueue.add(Constants.SET_CONTROLER_STATUS + "1;");
        }
    }
}
