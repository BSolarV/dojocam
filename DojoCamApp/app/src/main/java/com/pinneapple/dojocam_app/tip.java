package com.pinneapple.dojocam_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import org.jetbrains.annotations.NotNull;

public class tip extends AppCompatDialogFragment {
    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        //create view
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.tip_layout,null);
        TextView m = v.findViewById(R.id.textView13);
        m.setText("Aqui AGREGAR TIP");
        //button listener
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("TAG","you clicked the button");
            }
        };
        //build alert dialog
        return new AlertDialog.Builder(getActivity()).setTitle("changing message")
                .setView(v).setPositiveButton(android.R.string.ok,listener).create();
    }
}
