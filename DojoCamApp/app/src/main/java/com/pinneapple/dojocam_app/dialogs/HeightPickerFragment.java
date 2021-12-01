package com.pinneapple.dojocam_app.dialogs;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.pinneapple.dojocam_app.R;

public class HeightPickerFragment extends DialogFragment{

    private NumberPicker.OnValueChangeListener listener;

    public static Integer DEFAULT_VALUE = 75;

    public static HeightPickerFragment newInstance( NumberPicker.OnValueChangeListener listener) {
        HeightPickerFragment fragment = new HeightPickerFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(NumberPicker.OnValueChangeListener listener) {
        this.listener = listener;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LinearLayout linearLayout = new LinearLayout(requireContext());
        final NumberPicker myNumberPicker = new NumberPicker(requireContext());
        myNumberPicker.setMinValue(50);
        myNumberPicker.setMaxValue(300);
        myNumberPicker.setValue(DEFAULT_VALUE);
        myNumberPicker.setOnValueChangedListener(listener);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50, 1);
        RelativeLayout.LayoutParams numPickerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        numPickerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        TextView cmLabel = new TextView(requireContext());
        cmLabel.setText(R.string.heightUnit);

        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.addView(myNumberPicker, numPickerParams);
        linearLayout.addView(cmLabel, numPickerParams);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.RegisterHeightLabel);
        builder.setView(linearLayout);
        builder
                .setCancelable(false)
                .setPositiveButton(R.string.accept,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void saveData(){}

}