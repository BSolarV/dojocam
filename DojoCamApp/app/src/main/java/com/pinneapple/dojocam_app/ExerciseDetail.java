package com.pinneapple.dojocam_app;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExerciseDetail#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExerciseDetail extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView title ,desc;
    VideoView vid;
    private String videoId;
    private LoadingDialog loadingDialog = new LoadingDialog(this);
    private ProgressBar progressBar;


    public ExerciseDetail() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExerciseDetail.
     */
    // TODO: Rename and change types and number of parameters
    public static ExerciseDetail newInstance(String param1, String param2) {
        ExerciseDetail fragment = new ExerciseDetail();
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
        /*videoView = (VideoView) getActivity().findViewById(R.id.videoView);  //casting to VideoView is not Strictly required above API level 26
        String videoPath = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.braceadas_defensivas1;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri); //set the path of the video that we need to use in our VideoView
        videoView.start();  //start() method of the VideoView class will start the video to play*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_exercise_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videoId = getArguments().getString("videoId");

        title = (TextView) getView().findViewById(R.id.excersiceTitle);
        desc = (TextView)  getView().findViewById(R.id.textView10);

        Button pri = (Button) getView().findViewById(R.id.button);
        pri.setOnClickListener((View.OnClickListener) this);

        vid = (VideoView) getView().findViewById(R.id.videoView);
        /*String vid_path = "android.resource://" + getContext().getPackageName() +"/" + R.raw.braceadas_defensivas1;
        Uri uri = Uri.parse(vid_path);
        vid.setVideoURI(uri);

        MediaController mediaController = new MediaController(getContext());
        vid.setMediaController(mediaController);
        mediaController.setAnchorView(getView());*/
        //Progress Bar

        progressBar = (ProgressBar) getView().findViewById(R.id.progressbar);
        progressBar.bringToFront();
        progressBar.setVisibility(View.VISIBLE);


        MediaController mediaController = new MediaController(getContext());
        vid.setMediaController(mediaController);
        vid.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                mp.start();
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int arg1,
                                                   int arg2) {
                        // TODO Auto-generated method stub
                        progressBar.setVisibility(View.GONE);
                        //vid.setVisibility(View.VISIBLE);
                        mp.start();
                    }
                });
            }
        });
        mediaController.setAnchorView(getView());
        //mediaController.show();
        loadingDialog.startLoadingDialog();
    }

    @Override
    public void onClick(View view) {
        /*Intent mainActivity = new Intent(getContext(), Ml_model.class);
        startActivity(mainActivity);
        getActivity().finish();*/ // DESCOMENTAR MAS TARDE


        DocumentReference data = db.collection("ejercicios").document(videoId);
        Intent videoPip = new Intent(getContext(),PipActivity.class);
        videoPip.putExtra("videoUrl","https://videocdn.bodybuilding.com/video/mp4/62000/62792m.mp4");
        startActivity(videoPip);
        //Navigation.findNavController(view).navigate(R.id.practice);
    }

    @Override
    public void onResume() {

        super.onResume();


        // Get post and answers from database

        Task<DocumentSnapshot> data = db.collection("ejercicios").document(videoId).get();
        data.addOnSuccessListener(command -> {
            title.setText(command.get("nombre").toString());
            desc.setText(command.get("descripcion").toString());
            //vid.setText(command.get("authorEmail").toString());
            String vid_path = command.get("vid_path").toString();
            //String vid_path = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.braceadas_defensivas1;
            Uri uri = Uri.parse(vid_path);
            vid.setVideoURI(uri);
            //vid.start();
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
}