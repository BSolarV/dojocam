package com.pinneapple.dojocam_app.ui.dashboard;


import static android.content.ContentValues.TAG;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;


import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.FragmentDashboardBinding;
import com.pinneapple.dojocam_app.objects.VideoInfo;
import com.pinneapple.dojocam_app.objets.UserData;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;

import com.pinneapple.dojocam_app.GroupList;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.FragmentDashboardBinding;
import com.pinneapple.dojocam_app.tip;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class DashboardFragment extends Fragment implements View.OnClickListener {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView name, dificultad;
    private ImageView img;
    String whereQ = "basico";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        DocumentReference userReference = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        userReference.get().addOnSuccessListener(command -> {
                    UserData user = command.toObject(UserData.class);
                    assert user != null;

                    if( user.getLastExercise() == null ) {
                        whereQ = "basico";
                    } else {
                        whereQ = user.getLastExercise();
                    }

                });


        Task<QuerySnapshot> data = db.collection("ejercicios").whereEqualTo("dificultad", whereQ).get();
        data.addOnSuccessListener(command -> {


            List<VideoInfo> docList = command.toObjects(VideoInfo.class);
            Random rand = new Random();
            VideoInfo exercise = docList.get(rand.nextInt(docList.size()));
            name = (TextView) getView().findViewById(R.id.textView6);
            dificultad = (TextView) getView().findViewById(R.id.textView7);
            img = (ImageView) getView().findViewById(R.id.imageView6);
            name.setText(exercise.getNombre());
            String diffName="";
            switch (exercise.getDificultad()) {
                case "basico":
                    diffName = "Básico";
                    break;
                case "medio":
                    diffName = "Intermedio";
                    break;
                case "avanzado":
                    diffName = "Avanzado";
                    break;
            }
            dificultad.setText("Dificultad: " + diffName);
            /*Resources res = getResources();
            Drawable drawable = ResourcesCompat.getDrawable(res, R.drawable.default_recomendado, null);
            img.setImageDrawable(drawable);*/

        });

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
        Random r = new Random();
        int il = r.nextInt(100);
        if(il > 70){
            FragmentManager manager = getActivity().getSupportFragmentManager();
            tip dialog = new tip();
            dialog.show(manager,"message dialog");
        }
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