package com.pinneapple.dojocam_app;

import android.app.AlertDialog;
import android.view.LayoutInflater;

import androidx.fragment.app.Fragment;

public class LoadingDialog {
    Fragment fragment;
    AlertDialog dialog;

    public LoadingDialog(Fragment myFragment) {
        fragment = myFragment;
    }

    public void startLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getActivity());

        LayoutInflater inflater = fragment.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_layout, null));
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.show();
    }
    public void dismissDialog() {
        dialog.dismiss();
    }
}
