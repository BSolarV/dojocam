package com.pinneapple.dojocam_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectDificulty#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectDificulty extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SelectDificulty() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectDificulty.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectDificulty newInstance(String param1, String param2) {
        SelectDificulty fragment = new SelectDificulty();
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
        return inflater.inflate(R.layout.fragment_select_dificulty, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView pri = (TextView) getView().findViewById(R.id.textView);
        ImageView pri_img = (ImageView) getView().findViewById(R.id.imageView);
        TextView sec = (TextView) getView().findViewById(R.id.textView2);
        ImageView sec_img = (ImageView) getView().findViewById(R.id.imageView2);
        TextView thi = (TextView) getView().findViewById(R.id.textView3);
        ImageView thi_img = (ImageView) getView().findViewById(R.id.imageView3);

        pri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate(view, "basico");
            }
        });
        pri_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate(view, "basico");
            }
        });
        sec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate(view, "intermedio");
            }
        });
        sec_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate(view, "intermedio");
            }
        });
        thi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate(view, "avanzado");
            }
        });
        thi_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigate(view, "avanzado");
            }
        });
    }

    /*
    @Override
    public void onClick(View view) {

    }*/

    public void navigate(View view, String difficulty) {
        Bundle bundle = new Bundle();

        bundle.putString("difficulty" , difficulty);

        Navigation.findNavController(view).navigate(R.id.ejercicios, bundle);
    }
}