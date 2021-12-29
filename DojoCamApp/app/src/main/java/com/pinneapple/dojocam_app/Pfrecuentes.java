package com.pinneapple.dojocam_app;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pinneapple.dojocam_app.objets.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Pfrecuentes#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Pfrecuentes extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private List<String> bdQuestions = new ArrayList<>();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Pfrecuentes() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Pfrecuentes.
     */
    // TODO: Rename and change types and number of parameters
    public static Pfrecuentes newInstance(String param1, String param2) {
        Pfrecuentes fragment = new Pfrecuentes();
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
        return inflater.inflate(R.layout.fragment_pfrecuentes, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String tuString = "<b>¿Que es necesario para usar la Dojcam?</b>";
        TextView tv1 = (TextView)getView().findViewById(R.id.Preg1);
        tv1.setText(Html.fromHtml(tuString));

        String tuString2 = "<i>Poseer tu dispositivo movil en la posicion adecuada y tener al menos tu metro cuadrado libre, no queremos generar accidentes infortuitos</i>";
        TextView tv11 = (TextView)getView().findViewById(R.id.Res1);
        tv11.setText(Html.fromHtml(tuString2));


        String tuString3 = "<b>¿Por qué existe Dojocam?</b>";
        TextView tv2 = (TextView)getView().findViewById(R.id.Preg2);
        tv2.setText(Html.fromHtml(tuString3));

        String tuString31 = "<i>Porque necesitamos dar frente a los abusos</i>";
        TextView tv21 = (TextView)getView().findViewById(R.id.Res2);
        tv21.setText(Html.fromHtml(tuString31));

        String tuString4 = "<b>¿Como funciona el sistema de entrenamiento?</b>";
        TextView tv3 = (TextView)getView().findViewById(R.id.Preg3);
        tv3.setText(Html.fromHtml(tuString4));

        String tuString5 = "<i>A traves del video previo practico te demostramos que debes realizar, donde deberas seguir el esqueleto celeste </i>";
        TextView tv51 = (TextView)getView().findViewById(R.id.Res3);
        tv51.setText(Html.fromHtml(tuString5));

        Button btn = (Button) getView().findViewById(R.id.Prop_preg);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                alert.setTitle("Enviar Pregunta");
                alert.setMessage("");

                final EditText input = new EditText(getActivity());
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Editable value = input.getText();
                        //DocumentReference ref = mDatabase.getReference("BDQuestions");
                        DocumentReference userReference = db.collection("FAQ").document("BDQuestions");
                        bdQuestions.add(value.toString());
                        userReference.update("data",bdQuestions);

                        // Do something with value!
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        //consulta a bd
        DocumentReference userReference = db.collection("FAQ").document("BDQuestions");

        if (userReference != null) {
            userReference.get().addOnSuccessListener(command -> {
                bdQuestions = (List<String>) command.get("data");
            });
        }


    }
}