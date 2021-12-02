package com.pinneapple.dojocam_app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

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

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
    public void onResume() {
        super.onResume();
        
        DocumentReference qReference = db.collection("FAQ").document("Questions");
        qReference.get().addOnSuccessListener(command -> {
            List<String> ip = (List<String>) command.get("Data");
        });

        DocumentReference aReference = db.collection("FAQ").document("Answers");
        aReference.get().addOnSuccessListener(command -> {
            List<String> ip = (List<String>) command.get("Data");
        });
    }
}