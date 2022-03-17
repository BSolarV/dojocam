package com.pinneapple.dojocam_app;

import android.app.AlertDialog;
import android.view.LayoutInflater;

import androidx.fragment.app.Fragment;

public class LoadingDialog {
    Fragment fragment;
    AlertDialog dialog;
    private boolean status = false;

    public LoadingDialog(Fragment myFragment) {
        fragment = myFragment;
    }

    public void startLoadingDialog() {
        status = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());

        LayoutInflater inflater = fragment.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_layout, null));
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    public void dismissDialog() {
        status=false;
        dialog.dismiss();
    }

    public boolean getStatus(){
        return status;
    }
}
