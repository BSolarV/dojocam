package com.pinneapple.dojocam_app;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pinneapple.dojocam_app.Login.LoginActivity;
import com.pinneapple.dojocam_app.databinding.FragmentPerfilBinding;
import com.pinneapple.dojocam_app.objets.UserData;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Perfil#newInstance} factory method to
 * create an instance of this fragment.
 */

public class Perfil extends Fragment {

    private FragmentPerfilBinding binding;

    ImageView imageViewProfilePicture;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private LoadingDialog loadingDialog = new LoadingDialog(this);

    // Attributes
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Perfil() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Perfil.
     */
    // TODO: Rename and change types and number of parameters
    public static Perfil newInstance(String param1, String param2) {
        Perfil fragment = new Perfil();
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
        loadingDialog.startLoadingDialog();

    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUp();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPerfilBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    void setUp(){
        DocumentReference userReference = db.collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
        userReference.get().addOnSuccessListener(command -> {
            UserData user = command.toObject(UserData.class);
            assert user != null;

            binding.ProfileFirstName.setText( user.getFirstName() );
            binding.ProfileLastName.setText( user.getLastName() );

            // Sex Spinner
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                    R.array.sexOptions, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            binding.ProfileSexSpinner.setAdapter(adapter);
            binding.ProfileSexSpinner.setEnabled(false);
            binding.ProfileSexSpinner.setSelection( user.getSex() );

            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            binding.ProfileBirthDate.setText( formatter.format( user.getBirthDate() ) );
            binding.ProfileHeight.setText( user.getHeight().toString() );
            binding.ProfileWeight.setText( user.getWeight().toString() );
            loadingDialog.dismissDialog();
        });


        // Logout
        Button logout = (Button) getView().findViewById(R.id.ProfileLogoutButton);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent loginActivity = new Intent(getContext(), LoginActivity.class);
                startActivity(loginActivity);
                getActivity().finish();
            }
        });
        Button update_inf = (Button) getView().findViewById(R.id.ProfileUpdatebutton);

        update_inf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Editable height = binding.ProfileHeight.getText();
                Editable weight = binding.ProfileWeight.getText();

                UpdateData(height,weight);
            }
        });
        imageViewProfilePicture = getView().findViewById(R.id.ProfileImage);

        imageViewProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseProfilePicture();
            }
        });
    }

    private void UpdateData(Editable height, Editable weight) {
        DocumentReference userReference = db.collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
        System.out.println(height);
//DocumentReference userReference2 = db.collection("Users").document("bastian.vivar@sansano.usm.cl");
//DocumentReference docRef = db.collection("cities").document("DC");
        userReference.update("height",Integer.parseInt(String.valueOf(height)));
        userReference.update("weight",Integer.parseInt(String.valueOf(weight)));

    }


    private void chooseProfilePicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dalog_perfil, null);
        builder.setCancelable(false);
        builder.setView(dialogView);

        ImageView imageViewADPPCamera = dialogView.findViewById(R.id.imageViewADPPCamera);
        ImageView imageViewADPPGallery = dialogView.findViewById(R.id.imageViewADPPGallery);

        final AlertDialog alertDialogProfilePicture = builder.create();
        alertDialogProfilePicture.show();

        imageViewADPPGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePictureFromGallery();
                alertDialogProfilePicture.dismiss();
            }
        });
        imageViewADPPCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePictureFromCamera();
                alertDialogProfilePicture.dismiss();
            }
        });
    }
    private void takePictureFromGallery(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);
    }

    private void takePictureFromCamera(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture.resolveActivity(getActivity().getPackageManager()) != null){
            startActivityForResult(takePicture, 2);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImageUri = data.getData();
                    imageViewProfilePicture.setImageURI(selectedImageUri);
                }
                break;
            case 2:
                if(resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    Bitmap bitmapImage = (Bitmap) bundle.get("data");
                    imageViewProfilePicture.setImageBitmap(bitmapImage);
                }
                break;
        }
    }

    private boolean checkAndRequestPermissions(){
        if(Build.VERSION.SDK_INT >= 23){
            int cameraPermission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
            if(cameraPermission == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 20);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 20 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            takePictureFromCamera();
        }
        else
            Toast.makeText(getActivity(), "Permission not Granted", Toast.LENGTH_SHORT).show();
    }

}