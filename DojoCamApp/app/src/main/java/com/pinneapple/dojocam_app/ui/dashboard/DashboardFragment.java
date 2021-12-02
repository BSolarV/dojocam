package com.pinneapple.dojocam_app.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.pinneapple.dojocam_app.GroupList;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.FragmentDashboardBinding;

import org.jetbrains.annotations.NotNull;

public class DashboardFragment extends Fragment implements View.OnClickListener {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // final TextView textView = binding.textDashboard;
        // dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
        //     @Override
        //     public void onChanged(@Nullable String s) {
        //         textView.setText(s);
        //     }
        // });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tec = (TextView) getView().findViewById(R.id.textView11);
        ImageView tec_img = (ImageView) getView().findViewById(R.id.imageView7);

        TextView fis = (TextView) getView().findViewById(R.id.textView12);
        ImageView fis_img = (ImageView) getView().findViewById(R.id.imageView8);

        tec.setOnClickListener((View.OnClickListener) this);
        tec_img.setOnClickListener((View.OnClickListener) this);
        fis.setOnClickListener((View.OnClickListener) this);
        fis_img.setOnClickListener((View.OnClickListener) this);


        //Esto manda al fragment de exercise detail
        TextView one = (TextView) getView().findViewById(R.id.textView6);
        TextView two = (TextView) getView().findViewById(R.id.textView7);
        ImageView three = (ImageView) getView().findViewById(R.id.imageView6);

        Button btn = (Button) getView().findViewById(R.id.button2);
        Button chat = (Button) getView().findViewById(R.id.button6);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), GroupList.class);
                startActivity(i);
            }
        });


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();

                bundle.putString("difficulty" , "basico");
                bundle.putString("videoId", "G4Amg9DogfEr8jbRC6cO");
                bundle.putString("name", "Defensa alta");

                Navigation.findNavController(view).navigate(R.id.exerciseDetail, bundle);


            }
        });

        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();

                bundle.putString("difficulty" , "basico");
                bundle.putString("videoId", "G4Amg9DogfEr8jbRC6cO");
                bundle.putString("name", "Defensa alta");

                Navigation.findNavController(view).navigate(R.id.exerciseDetail, bundle);

            }
        });
        two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();

                bundle.putString("difficulty" , "basico");
                bundle.putString("videoId", "G4Amg9DogfEr8jbRC6cO");
                bundle.putString("name", "Defensa alta");

                Navigation.findNavController(view).navigate(R.id.exerciseDetail, bundle);

            }
        });
        three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();

                bundle.putString("difficulty" , "basico");
                bundle.putString("videoId", "G4Amg9DogfEr8jbRC6cO");
                bundle.putString("name", "Defensa alta");

                Navigation.findNavController(view).navigate(R.id.exerciseDetail, bundle);

            }
        });




    }
    @Override
    public void onClick(View view) {

        Navigation.findNavController(view).navigate(R.id.selectDificulty);
    }
}