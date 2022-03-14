package com.pinneapple.dojocam_app;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

//import static io.grpc.Context.LazyStorage.storage;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;

import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pinneapple.dojocam_app.Login.LoginActivity;
import com.pinneapple.dojocam_app.databinding.FragmentPerfilBinding;
import com.pinneapple.dojocam_app.objets.UserData;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Perfil#newInstance} factory method to
 * create an instance of this fragment.
 */

public class Perfil extends Fragment {

    private FragmentPerfilBinding binding;

    ImageView imageViewProfilePicture;

    SharedPreferences sharedPreferences;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();

    UserData user;

    private String imageId;
    private String image_path;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private LoadingDialog loadingDialog = new LoadingDialog(this);

    // Attributes
    StorageReference sref = FirebaseStorage.getInstance().getReference();
    StorageReference mStorageReference;

    private FragmentActivity myContext;

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
        sharedPreferences = requireActivity().getSharedPreferences(requireContext().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        loadingDialog.startLoadingDialog();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }
        setUp();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {

        inflater.inflate(R.menu.perfil_menu, menu);
        MenuItem s = menu.findItem(R.id.menuNavigation);
        s.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Navigation.findNavController(getView()).navigate(R.id.pfrecuentes);
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);

    }

    void setUp(){
        DocumentReference userReference = db.collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
        //Uri downloadURI = sref.child(Objects.requireNonNull("images/"+FirebaseAuth.getInstance().getCurrentUser().getEmail())+".jpg").getDownloadUrl().getResult();


        userReference.get().addOnSuccessListener(command -> {
            user = command.toObject(UserData.class);
            assert user != null;

            binding.ProfileFirstName.setText( user.getFirstName() );
            binding.ProfileLastName.setText( user.getLastName());
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

            // Set up the ImageView
            String imageId = user.getImageName();
            if(imageId != null) {
                retrieveImagePath(requireContext(), imageId);
            }else {
                binding.ProfileImage.setImageResource(R.drawable.ic_usr_no_image);
            }

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
                Editable firstName = binding.ProfileFirstName.getText();
                Editable lastName = binding.ProfileLastName.getText();

                UpdateData(height,weight,firstName,lastName);
            }
        });

        Button addamigo = (Button) getView().findViewById(R.id.AddFriend2);
        addamigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.AddFriend);
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
    private Bitmap getImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Error getting bitmap", e);
        }
        return bm;
    }
    private void UpdateData(Editable height, Editable weight, Editable firstName, Editable lastName) {
        DocumentReference userReference = db.collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail()));

        userReference.update("height",Integer.parseInt(String.valueOf(height)));
        userReference.update("weight",Integer.parseInt(String.valueOf(weight)));
        userReference.update("firstName",(String.valueOf(firstName)));
        userReference.update("lastName",(String.valueOf(lastName)));
    }

    private void chooseProfilePicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dalog_perfil, null);
        builder.setCancelable(false);
        builder.setView(dialogView);

        ImageView imageViewADPPCamera = dialogView.findViewById(R.id.imageViewADPPCamera);
        ImageView imageViewADPPGallery = dialogView.findViewById(R.id.imageViewADPPGallery);
        ImageView background = dialogView.findViewById(R.id.notification_background);

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
        background.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //SetBackground();
                // here I want to dismiss it after SetBackground() method
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
        Boolean result = checkAndRequestPermissions();

        if (result && takePicture.resolveActivity(getActivity().getPackageManager()) != null) {
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
                    uploadImage(selectedImageUri);
                }
                break;
            case 2:
                if(resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    Log.wtf("Bundle", bundle.toString());
                    Bitmap bitmapImage = (Bitmap) bundle.get("data");
                    imageViewProfilePicture.setImageBitmap(bitmapImage);
                    uploadFile(bitmapImage);
                }
                break;
        }
    }
    private void uploadFile(Bitmap bitmap) {
        FirebaseUser FBUser = FirebaseAuth.getInstance().getCurrentUser();
        if (FBUser == null) {
            Intent loginActivity = new Intent(requireActivity(), LoginActivity.class);
            startActivity(loginActivity);
            requireActivity().finish();
        } else {
            String email = FBUser.getEmail();
            Date date = new Date();
            String imageId = createHash256(email + date.toString());

            // Save image in to user

            user.setImageName(imageId);
            Task<Void> result = db.collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getEmail())).update("imageName", imageId);
            result.addOnCompleteListener(task -> {Log.wtf("Update", "Completed: "+task);});
            result.addOnSuccessListener(task -> {Log.wtf("Update", "Image name updated");});
            result.addOnFailureListener(task -> {Log.wtf("Update", "Image name not updated: "+task.getMessage());});

            // Save image to Firebase Storage
            StorageReference storageRef = storage.getReferenceFromUrl("gs://dojocam-app.appspot.com");
            StorageReference ImagesRef = storageRef.child("Images/" + imageId + ".jpg");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = ImagesRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getActivity(), "No se logro actaulizar", Toast.LENGTH_SHORT).show();
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast
                            .makeText(getActivity(),
                                    "Actualizado",
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
    }
    private void uploadImage(Uri selectedImageUri)
    {
        FirebaseUser FBUser = FirebaseAuth.getInstance().getCurrentUser();
        if (FBUser == null) {
            Intent loginActivity = new Intent(requireActivity(), LoginActivity.class);
            startActivity(loginActivity);
            requireActivity().finish();
        } else {
            String email = FBUser.getEmail();
            Date date = new Date();
            String imageId = createHash256(email+date.toString());

            if (selectedImageUri != null) {

                // Code for showing progressDialog while uploading
                ProgressDialog progressDialog
                        = new ProgressDialog(getActivity());
                progressDialog.setTitle("Uploading...");
                progressDialog.show();

                // Save image in to user
                user.setImageName(imageId);

                assert email != null;
                db.collection("Users").document(email).update("imageName", imageId)
                .addOnCompleteListener(task -> {Log.wtf("Update", "Completed: "+task.getResult());})
                .addOnSuccessListener(task -> {Log.wtf("Update", "Image name updated");})
                .addOnFailureListener(task -> {Log.wtf("Update", "Image name not updated: "+task.getMessage());});

                // Save image to Firebase Storage
                StorageReference storageRef = storage.getReferenceFromUrl("gs://dojocam-app.appspot.com");

                StorageReference ImagesRef = storageRef.child("Images/" + imageId + ".jpg");
                ImagesRef.putFile(selectedImageUri)
                        .addOnSuccessListener(
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(
                                            UploadTask.TaskSnapshot taskSnapshot) {
                                        // Image uploaded successfully
                                        // Dismiss dialog
                                        progressDialog.dismiss();
                                        Toast
                                                .makeText(getActivity(),
                                                        "Actualizado",
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error, Image not uploaded
                                progressDialog.dismiss();
                                Toast
                                        .makeText(getActivity(),
                                                "Failed " + e.getMessage(),
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
            }
        }
    }

    private boolean checkAndRequestPermissions(){
        try {
            int cameraPermission = ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA);
            if(cameraPermission == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, 20);
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.wtf("Error", e);
            return false;
        }
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

    /* Save Image in cache */

    private String createHash256(String text) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");

            md.update(text.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();

            return String.format("%064x", new BigInteger(1, digest));

        } catch (NoSuchAlgorithmException e) {
            Log.wtf("Hash256", e.getMessage());
            return null;
        }
    }

    public void retrieveImagePath(Context context, String imageId) {
        if( sharedPreferences.contains(imageId) ){

            String image_path = sharedPreferences.getString(imageId, null);
            Uri uri = Uri.parse( image_path );
            imageViewProfilePicture.setImageURI(uri);

        }else{
            downloadFile(context, imageId);
        }
    }
    private void downloadFile(Context context, String imageId) {
        try {
            String rootDir = context.getCacheDir()
                    + File.separator + "ProfileImages";
            File rootFile = new File(rootDir);
            if(!rootFile.exists()) {
                rootFile.mkdirs();
            }

            StorageReference reference = storage.getReference();
            StorageReference fileReference = reference.child("Images/"+imageId+".jpg");

            final File localFile = new File(rootFile,imageId+".jpg");


            fileReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.wtf("FIREBASE!", ";local tem file created  created " + localFile.toString());
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(imageId, localFile.getPath());
                            editor.apply();

                            String image_path = localFile.getPath();
                            Uri uri = Uri.parse( image_path );
                            imageViewProfilePicture.setImageURI(uri);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.wtf("FIREBASE!", "No se pudo guardar la imagen en caché: " + exception.toString());
                            assert getFragmentManager() != null;
                            getFragmentManager().popBackStack();
                        }
                    });



        } catch (Exception e) {
            Log.wtf("Error....", e.toString());
            getFragmentManager().popBackStack();
        }
    }

}