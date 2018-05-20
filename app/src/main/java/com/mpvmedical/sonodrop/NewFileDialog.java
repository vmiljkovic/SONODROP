package com.mpvmedical.sonodrop;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;


public class NewFileDialog extends DialogFragment {

    public interface NoticeNewFileDialogListener {
        void onNewFileDialogPositiveClick(DialogFragment dialog);
    }

    public WorkingSettings mWorkingSettings = null;

    NoticeNewFileDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (NoticeNewFileDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeNewFileDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.edit_file_name, null))
                .setPositiveButton(getResources().getString(R.string.savebtn), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText filename = (EditText) getDialog().findViewById(R.id.filename);
                        WorkingSettings.saveWorkingSettings(getContext(), mWorkingSettings, filename.getText().toString());
                        mListener.onNewFileDialogPositiveClick(NewFileDialog.this);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancelbtn), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NewFileDialog.this.getDialog().cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
        Button n = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        n.setTextColor(getResources().getColor(R.color.colorPrimary));
        Button p = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        p.setTextColor(getResources().getColor(R.color.colorPrimary));
        return alert;
    }
}
