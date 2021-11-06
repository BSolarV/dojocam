package com.pinneapple.dojocam_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExerciseDetail#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExerciseDetail extends Fragment implements View.OnClickListener {

    private SharedPreferences sharedPreferences;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage;

    private LoadingDialog loadingDialog = new LoadingDialog(this);
    private ProgressBar progressBar;

    private TextView title ,desc;
    private VideoView vid;

    private String videoId;
    private String namefile;
    private String vid_path;



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
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = requireContext().getSharedPreferences(
                requireContext().getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        storage = FirebaseStorage.getInstance();

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
        if( vid_path != null ){
            Bundle bundle = new Bundle();
            bundle.putString("namefile", namefile);
            bundle.putString("vid_path", vid_path);
            Intent mainActivity = new Intent(getContext(), Ml_model.class);
            mainActivity.putExtras(bundle);
            startActivity(mainActivity);
        }

        //getActivity().finish();

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
            namefile = command.get("id").toString();

            setVideoPath(requireContext(), namefile);

            // vid_path = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.braceadas_defensivas1;

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

    public void setVideoPath(Context context, String videoId ) {
        if( sharedPreferences.contains(videoId) ){

            vid_path = sharedPreferences.getString(videoId, null);
            Uri uri = Uri.parse( vid_path );
            vid.setVideoURI(uri);

        }else{
            downloadFile(context, videoId);
        }
    }
    private void downloadFile(Context context, String videoId) {
        try {
            String rootDir = context.getCacheDir()
                    + File.separator + "Videos";
            File rootFile = new File(rootDir);
            if(!rootFile.exists()) {
                rootFile.mkdirs();
            }

            StorageReference reference = storage.getReference();
            StorageReference fileReference = reference.child("Videos/"+videoId+".mp4");

            final File localFile = new File(rootFile,videoId+".mp4");

            fileReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.wtf("FIREBASE!", ";local tem file created  created " + localFile.toString());
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(videoId, localFile.getPath());
                            editor.commit();

                            vid_path = localFile.getPath();
                            Uri uri = Uri.parse( vid_path );
                            vid.setVideoURI(uri);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.wtf("FIREBASE!", ";local tem file not created  created " + exception.toString());
                            getFragmentManager().popBackStack();
                        }
                    });



        } catch (Exception e) {
            Log.wtf("Error....", e.toString());
            getFragmentManager().popBackStack();
        }
    }

}