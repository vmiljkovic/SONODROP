package com.mpvmedical.sonodrop;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;

import java.util.ArrayList;

public class LoadFileDialog extends DialogFragment {

    public ArrayList<String> mFiles = new ArrayList<>();

    public interface NoticeLoadFileDialogListener {
        void onLoadFileDialogPositiveClick(DialogFragment dialog, int which);
    }

    NoticeLoadFileDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (NoticeLoadFileDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeLoadFileDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setTitle(getResources().getString(R.string.loadfiletitle))
                .setItems(mFiles.toArray(new String[mFiles.size()]), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onLoadFileDialogPositiveClick(LoadFileDialog.this, which);
                    }
                });

        return builder.create();
    }
}
