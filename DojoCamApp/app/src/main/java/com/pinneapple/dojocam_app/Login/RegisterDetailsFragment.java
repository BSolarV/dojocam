package com.pinneapple.dojocam_app.Login;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.pinneapple.dojocam_app.LoadingDialog;
import com.pinneapple.dojocam_app.MainActivity;
//import com.pinneapple.dojocam_app.Ml_model;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.dialogs.DatePickerFragment;
import com.pinneapple.dojocam_app.dialogs.HeightPickerFragment;
import com.pinneapple.dojocam_app.dialogs.WeightPickerFragment;
import com.pinneapple.dojocam_app.objets.Friends;
import com.pinneapple.dojocam_app.objets.UserData;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterDetailsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String GSINGIN = "GSINGIN";
    public static final String GCREDENTIAL= "GCREDENTIAL";
    public static final String EMAIL = "EMAIL";
    public static final String PASSWORD = "PASSWORD";

    // TODO: Rename and change types of parameters
    private Boolean GSingIn = false;
    private AuthCredential GCredential;
    private String mEMAIL;
    private String mPASSWORD;

    // Attributes
    private final LoadingDialog loadingDialog = new LoadingDialog(this);
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText name;
    private EditText lastname;
    private Spinner sex;
    private TextView birthdate;
    private Date birthDateValue;
    private TextView height;
    private TextView weight;
    private Button save;

    public RegisterDetailsFragment() {
        // Required empty public constructor
    }

    public static RegisterDetailsFragment newInstance(Boolean GSingIn, AuthCredential GCredential, String  email, String password) {
        RegisterDetailsFragment fragment = new RegisterDetailsFragment();
        Bundle args = new Bundle();
        args.putBoolean(GSINGIN, GSingIn);
        args.putParcelable(GCREDENTIAL, GCredential);
        args.putString(EMAIL, email);
        args.putString(PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }

    public static RegisterDetailsFragment newInstance(String  email, String password) {
        return newInstance(false, null, email, password);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Log.wtf("REG:", String.valueOf(getArguments().getBoolean(GSINGIN)));
            GSingIn = getArguments().getBoolean(GSINGIN);
            GCredential = getArguments().getParcelable(GCREDENTIAL);
            mEMAIL = getArguments().getString(EMAIL);
            mPASSWORD = getArguments().getString(PASSWORD);
        }
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        name = view.findViewById( R.id.RegisterFirstName );
        lastname = view.findViewById( R.id.RegisterLastName );
        sex = view.findViewById( R.id.RegisterSexSpinner );
        birthdate = view.findViewById( R.id.RegisterBirthDate );
        height = view.findViewById( R.id.RegisterHeight );
        weight = view.findViewById( R.id.RegisterWeight );
        save = view.findViewById(R.id.RegisterDetailSubmit);

        birthdate.setText(R.string.RegisterDefaultDate);
        height.setText(R.string.DefaultNumber);
        weight.setText(R.string.DefaultNumber);


        // Sex Spinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sexOptions, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sex.setAdapter(adapter);

        //DatePickerSetup
        birthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });

        //heightPickerSetup
        height.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHeightPickerDialog(v);
            }
        });

        //weightPickerSetup
        weight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWeightPickerDialog(v);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInformation();

            }
        });

    }

    private void saveInformation() {
        UserData user = new UserData(
                name.getText().toString(),
                lastname.getText().toString(),
                sex.getSelectedItemPosition(),
                birthDateValue,
                Integer.valueOf(height.getText().toString()),
                Integer.valueOf(weight.getText().toString()));

        loadingDialog.startLoadingDialog();

        if( GSingIn ){
            FirebaseAuth.getInstance().signInWithCredential(GCredential)
                    .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser FBUser = task.getResult().getUser();
                                assert FBUser != null;


                                TextView email = getView().findViewById( R.id.RegisterEmail );
                                /*
                                System.out.println("Hola aqui nos vamos\n");
                                System.out.println(FBUser);
                                Friends followers = new Friends();
                                followers.add("basty@jeje.com");
                                System.out.println("COmo SHoro\n");

                                System.out.println(Objects.requireNonNull(FBUser.getEmail()));
                                System.out.println(email);

                                db.collection("Friends").document(Objects.requireNonNull(FBUser.getEmail()))
                                        .set(followers)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Guardado la lista de amigos vacia");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "No se logro Guardar", e);
                                            }
                                        });
                                System.out.println("Hola aqui nos vamos");
                                */
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name.getText().toString() + lastname.getText().toString())
                                        .build();
                                FBUser.updateProfile(profileUpdates);
                                db.collection("Users").document(Objects.requireNonNull(FBUser.getEmail())).set(user)
                                        .addOnCompleteListener(task1 -> {
                                            loadingDialog.dismissDialog();
                                            toMainActivity();
                                        }
                                        )
                                .addOnFailureListener(result -> {
                                    loadingDialog.dismissDialog();
                                    Log.wtf("Error Register", result.getMessage());
                                    Toast.makeText(requireActivity(), "No se pudo guardar la información, revise su conexión a internet.", Toast.LENGTH_SHORT).show();
                                });

                            } else {
                                // If sign in fails, display a message to the user.
                                loadingDialog.dismissDialog();
                                Toast.makeText(requireActivity(), "No se pudo guardar la información, revise su conexión a internet.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(mEMAIL, mPASSWORD)
                    .addOnSuccessListener(command -> {
                        if( command.getUser() == null || command.getUser().getEmail() == null ){
                            loadingDialog.dismissDialog();
                            Toast.makeText(requireContext(), "Error: No se pudo crear su cuenta. Por favor intente nuevamente", Toast.LENGTH_LONG).show();
                        } else {
                            FirebaseUser FBUser = command.getUser();
                            assert FBUser != null;
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name.getText().toString() + lastname.getText().toString())
                                    .build();
                            FBUser.updateProfile(profileUpdates);
                            db.collection("Users").document(Objects.requireNonNull(FBUser.getEmail())).set(user);
                            loadingDialog.dismissDialog();
                            toMainActivity();
                        }
                    })
                    .addOnFailureListener(command -> {
                        loadingDialog.dismissDialog();
                        Toast.makeText(requireContext(), "Error: "+command.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_details, container, false);
    }

    public void showDatePickerDialog(View v) {
        TextView birthdate = requireView().findViewById( R.id.RegisterBirthDate );
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = new GregorianCalendar(year, month, dayOfMonth);
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                birthdate.setText( formatter.format(calendar.getTime()) );
                birthDateValue = calendar.getTime();
            }
        });
        if( birthdate.getText().toString() == getResources().getString(R.string.RegisterDefaultDate) ){
            newFragment.show(requireActivity().getSupportFragmentManager(), String.valueOf(R.string.RegisterBirthDateLabel));
            Date dateNow = new Date();
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            birthdate.setText( formatter.format( dateNow ));
            birthDateValue = dateNow;
        }
    }

    public void showHeightPickerDialog(View v) {
        TextView height = requireView().findViewById(R.id.RegisterHeight);
        DialogFragment newFragment = HeightPickerFragment.newInstance(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                height.setText(String.valueOf(newVal));
            }
        });
        newFragment.show(requireActivity().getSupportFragmentManager(), String.valueOf(R.string.RegisterHeightLabel));
        if(height.getText().toString().equals(getResources().getString(R.string.DefaultNumber))){
            height.setText(String.valueOf(HeightPickerFragment.DEFAULT_VALUE));
        }
    }

    public void showWeightPickerDialog(View v) {
        TextView weight = requireView().findViewById(R.id.RegisterWeight);
        DialogFragment newFragment = WeightPickerFragment.newInstance(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                weight.setText(String.valueOf(newVal));
            }
        });
        newFragment.show(requireActivity().getSupportFragmentManager(), String.valueOf(R.string.RetgisterWeightLabel));
        if(weight.getText().toString().equals(getResources().getString(R.string.DefaultNumber))){
            weight.setText(String.valueOf(WeightPickerFragment.DEFAULT_VALUE));
        }
    }


    private void toMainActivity(){
        Intent mainActivity = new Intent(getContext(), MainActivity.class);
        startActivity(mainActivity);
        getActivity().finish();
    }
}