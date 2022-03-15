package com.pinneapple.dojocam_app.ui.dashboard;


import static android.content.ContentValues.TAG;

import android.app.Activity;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;


import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pinneapple.dojocam_app.Login.LoginActivity;
import com.pinneapple.dojocam_app.MainActivity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.pinneapple.dojocam_app.GroupList;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.FragmentDashboardBinding;
import com.pinneapple.dojocam_app.tip;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import java.util.Random;

public class DashboardFragment extends Fragment implements View.OnClickListener {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TextView name, dificultad;
    private ImageView img;
    private String whereQ = "basico";
    private String last_exercise;
    private int number;
    private VideoInfo exercise;
    private List<String> id_list = new ArrayList();

    private TextView one;
    private TextView two;
    private ImageView three;
    private Button btn;
    private Button chat;


    private TextView retomar;
    private ImageView retomar_img;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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


        //TextView tec = (TextView) getView().findViewById(R.id.textView11);
        //ImageView tec_img = (ImageView) getView().findViewById(R.id.imageView7);

        TextView tec = (TextView) getView().findViewById(R.id.textView12);
        ImageView tec_img = (ImageView) getView().findViewById(R.id.imageView8);


        TextView fis = (TextView) getView().findViewById(R.id.textView12);
        ImageView fis_img = (ImageView) getView().findViewById(R.id.imageView8);

        //tec.setOnClickListener((View.OnClickListener) this);
        //tec_img.setOnClickListener((View.OnClickListener) this);
        fis.setOnClickListener((View.OnClickListener) this);
        fis_img.setOnClickListener((View.OnClickListener) this);

        id_list.clear();

        DocumentReference userReference = db.collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());

        userReference.get().addOnSuccessListener(command -> {
            UserData user = command.toObject(UserData.class);
            assert user != null;

            if( user.getLastExercise() == null ) {
                whereQ = "basico";
            } else {
                whereQ = user.getLastExercise();
            }

            if( user.getLastExercisePath() == null ) {
                last_exercise = "";
            } else {
                last_exercise = user.getLastExercisePath();
            }



            //Last exercise
            retomar = (TextView) getView().findViewById(R.id.textView80);
            retomar_img = (ImageView) getView().findViewById(R.id.imageView9);


            if(last_exercise == null || last_exercise.equals("")) {
                retomar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(),"No hay ejercicios Pendientes", Toast.LENGTH_SHORT ).show();

                    }
                });
                retomar_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(),"No hay ejercicios Pendientes", Toast.LENGTH_SHORT ).show();

                    }
                });

            }
            else {
                Task<DocumentSnapshot> data2 = db.collection("ejercicios").document(last_exercise).get();
                data2.addOnSuccessListener(command2 -> {

                    retomar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Bundle bundle = new Bundle();

                            bundle.putString("difficulty" , command2.get("dificultad").toString());
                            bundle.putString("videoId", last_exercise);
                            bundle.putString("name", command2.get("nombre").toString());

                            Navigation.findNavController(view).navigate(R.id.exerciseDetail, bundle);

                        }
                    });
                    retomar_img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Bundle bundle = new Bundle();

                            bundle.putString("difficulty" , command2.get("dificultad").toString());
                            bundle.putString("videoId", last_exercise);
                            bundle.putString("name", command2.get("nombre").toString());

                            Navigation.findNavController(view).navigate(R.id.exerciseDetail, bundle);

                        }
                    });


                });
                /*data2.addOnFailureListener(error ->{
                    Toast.makeText(getContext(),"No hay ejercicios Pendientes", Toast.LENGTH_SHORT ).show();
                });*/

            }


        });


        Task<QuerySnapshot> data = db.collection("ejercicios").whereEqualTo("dificultad", whereQ).get();
        data.addOnSuccessListener(command -> {


            List<VideoInfo> docList = command.toObjects(VideoInfo.class);
            if ( data.isComplete() ) {
                int i = 0;
                for (VideoInfo videoInfo :
                        docList) {
                    id_list.add(command.getDocuments().get(i).getId());
                    i++;
                }
            }

            Random rand = new Random();

            number = rand.nextInt(docList.size());

            exercise = docList.get(number);
            name = (TextView) view.findViewById(R.id.textView6);
            dificultad = (TextView) view.findViewById(R.id.textView7);
            img = (ImageView) view.findViewById(R.id.imageView6);
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


            //Toast.makeText(getContext(), exercise.getId(), Toast.LENGTH_SHORT).show();

            //Esto manda al fragment de exercise detail
            one = (TextView) view.findViewById(R.id.textView6);
            two = (TextView) view.findViewById(R.id.textView7);
            three = (ImageView) view.findViewById(R.id.imageView6);

            btn = (Button) view.findViewById(R.id.button2);
            FloatingActionButton chat = view.findViewById(R.id.fab);
            chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getContext(), GroupList.class);
                    startActivity(i);
                }
            });


            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();

                    bundle.putString("difficulty" , exercise.getDificultad());
                    bundle.putString("videoId", id_list.get(number));
                    bundle.putString("name", exercise.getNombre());

                    Navigation.findNavController(view).navigate(R.id.exerciseDetail, bundle);


                }
            });

            one.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();

                    bundle.putString("difficulty" , exercise.getDificultad());
                    bundle.putString("videoId", id_list.get(number));
                    bundle.putString("name", exercise.getNombre());

                    Navigation.findNavController(view).navigate(R.id.exerciseDetail, bundle);

                }
            });
            two.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();

                    bundle.putString("difficulty" , exercise.getDificultad());
                    bundle.putString("videoId", id_list.get(number));
                    bundle.putString("name", exercise.getNombre());

                    Navigation.findNavController(view).navigate(R.id.exerciseDetail, bundle);

                }
            });
            three.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();

                    bundle.putString("difficulty" , exercise.getDificultad());
                    bundle.putString("videoId", id_list.get(number));
                    bundle.putString("name", exercise.getNombre());

                    Navigation.findNavController(view).navigate(R.id.exerciseDetail, bundle);

                }
            });

        });
    }

    @Override
    public void onClick(View view) {

        Navigation.findNavController(view).navigate(R.id.selectDificulty);
    }

    @Override
    public void onResume() {
        super.onResume();

    }


}