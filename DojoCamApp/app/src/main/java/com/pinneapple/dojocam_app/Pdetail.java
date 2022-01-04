package com.pinneapple.dojocam_app;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Pdetail#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Pdetail extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String id1;
    private String userId;
    private TextView res;
    private TextView pre;
    private LoadingDialog loadingDialog = new LoadingDialog(this);
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> newrate;
    private List<String> newrateNeg;
    private Button pri;
    private Button sec;
    public Pdetail() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Pdetail.
     */
    // TODO: Rename and change types and number of parameters
    public static Pdetail newInstance(String param1, String param2) {
        Pdetail fragment = new Pdetail();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pdetail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        id1 = getArguments().getString("Id");
        userId = getArguments().getString("userId");
        pre = (TextView) getView().findViewById(R.id.excersiceTitle);
        res = (TextView)  getView().findViewById(R.id.textView10);

        pri = (Button) getView().findViewById(R.id.button);
        pri.setBackgroundColor(0xfffafa);
        pri.setOnClickListener((View.OnClickListener) this);
        sec = (Button) getView().findViewById(R.id.buttonNeg);
        sec.setBackgroundColor(0xfffafa);
        sec.setOnClickListener((View.OnClickListener) this);


        loadingDialog.startLoadingDialog();
    }

    @Override
    public void onResume() {

        super.onResume();


        // Get post and answers from database

        Task<DocumentSnapshot> data = db.collection("FAQTest").document(id1).get();
        data.addOnSuccessListener(command -> {
            pre.setText(command.get("pregunta").toString());
            res.setText(command.get("respuesta").toString());
            newrate = (List<String>) command.get("rating");
            newrateNeg = (List<String>) command.get("ratingNeg");
            if(newrate.contains(userId))pri.setBackgroundColor(pri.getContext().getResources().getColor(R.color.purple_200));
            if(newrateNeg.contains(userId))sec.setBackgroundColor(sec.getContext().getResources().getColor(R.color.danger));

            pri.setText(newrate.size() + " les fue util");
            sec.setText(newrateNeg.size() + " no les gusto");

            loadingDialog.dismissDialog();

        });
        data.addOnFailureListener(command -> {
            loadingDialog.dismissDialog();
            //Toast.makeText(getContext(), "No te veo compare, avispateeee", Toast.LENGTH_SHORT).show();

            // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

// 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("Tiempo de espera excedido")
                    .setTitle("Error de Conexión");

// 3. Get the <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code> from <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
            AlertDialog dialog = builder.create();
            dialog.show();

        });
    }

    @Override
    public void onClick(View v) {
        DocumentReference userReference = db.collection("FAQTest").document(id1);
        Log.i("AAA",res.getText().toString());
        if(res.getText().toString().equals("-Pendiente") ){
            Log.i("AAA","SI ENTRO");
            Toast.makeText(getContext(), "No hay respuesta que evaluar", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()){
            case R.id.button:
                if(!newrate.contains(userId)){
                    newrate.add(userId);
                    pri.setBackgroundColor(pri.getContext().getResources().getColor(R.color.purple_200));
                    if(newrateNeg.contains(userId)) newrateNeg.remove(userId);
                    sec.setBackgroundColor(0xfffafa);
                    Toast.makeText(getContext(), "Marcaste esta respuesta como util", Toast.LENGTH_SHORT).show();
                } else{
                    pri.setBackgroundColor(0xfffafa);
                    newrate.remove(userId);
                }
                userReference.update("rating",newrate);
                userReference.update("ratingNeg",newrateNeg);
                break;
            case R.id.buttonNeg:
                if(!newrateNeg.contains(userId)){
                    newrateNeg.add(userId);
                    sec.setBackgroundColor(getContext().getResources().getColor(R.color.danger));
                    if(newrate.contains(userId)) newrate.remove(userId);
                    pri.setBackgroundColor(0xfffafa);
                    Toast.makeText(getContext(), "Marcaste que esta respuesta no es de ayuda", Toast.LENGTH_SHORT).show();
                } else{
                    sec.setBackgroundColor(0xfffafa);
                    newrateNeg.remove(userId);
                }
                userReference.update("rating",newrate);
                userReference.update("ratingNeg",newrateNeg);
                break;
        }
        pri.setText(newrate.size() + " les fue util");
        sec.setText(newrateNeg.size() + " no les gusto");
    }
}