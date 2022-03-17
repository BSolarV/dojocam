package com.pinneapple.dojocam_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
//import com.pinneapple.dojocam_app.databinding.FragmentPerfilBinding;
import com.pinneapple.dojocam_app.objets.UserData;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class tip extends AppCompatDialogFragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //private FragmentPerfilBinding binding;
    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        //create view
        Random r = new Random();
        int t = r.nextInt(15);
        DocumentReference userReference = db.collection("Consejos").document("0");
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.tip_layout,null);
        TextView m = v.findViewById(R.id.textView13);
        userReference.get().addOnSuccessListener(command -> {
            List<String> ip = (List<String>) command.get("Descripcion");
            m.setText(ip.get(t).toString());
        });
        //button listener
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("TAG","you clicked the button");
            }
        };
        //build alert dialog

        return new AlertDialog.Builder(getActivity()).setTitle("Consejo de Dojocam")
                .setView(v).setPositiveButton(android.R.string.ok,listener).create();
    }
}
