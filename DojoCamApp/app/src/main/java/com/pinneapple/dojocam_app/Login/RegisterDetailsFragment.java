package com.pinneapple.dojocam_app.Login;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.pinneapple.dojocam_app.MainActivity;
//import com.pinneapple.dojocam_app.Ml_model;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.dialogs.DatePickerFragment;
import com.pinneapple.dojocam_app.dialogs.HeightPickerFragment;
import com.pinneapple.dojocam_app.dialogs.WeightPickerFragment;
import com.pinneapple.dojocam_app.objets.UserData;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    // Attributes
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

    public static RegisterDetailsFragment newInstance(String param1) {
        RegisterDetailsFragment fragment = new RegisterDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                toMainActivity();
            }
        });

    }

    private void saveInformation() {
        UserData user = new UserData(
                name.getText().toString(),
                lastname.getText().toString(),
                sex.getSelectedItem().toString(),
                birthDateValue,
                Integer.valueOf(height.getText().toString()),
                Integer.valueOf(weight.getText().toString()));
        db.collection("Users").document( FirebaseAuth.getInstance().getCurrentUser().getEmail()).set(user);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_details, container, false);
    }


    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                TextView birthdate = requireView().findViewById( R.id.RegisterBirthDate );
                Calendar calendar = new GregorianCalendar(year, month, dayOfMonth);
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                birthdate.setText( formatter.format(calendar.getTime()) );
                birthDateValue = calendar.getTime();
            }
        });
        newFragment.show(requireActivity().getSupportFragmentManager(), String.valueOf(R.string.RegisterBirthDateLabel));
        birthDateValue = newFragment.getBirthDateValue();
    }

    public void showHeightPickerDialog(View v) {
        DialogFragment newFragment = HeightPickerFragment.newInstance(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                TextView height = requireView().findViewById(R.id.RegisterHeight);
                height.setText(String.valueOf(newVal));
            }
        });
        newFragment.show(requireActivity().getSupportFragmentManager(), String.valueOf(R.string.RegisterHeightLabel));
    }

    public void showWeightPickerDialog(View v) {
        DialogFragment newFragment = WeightPickerFragment.newInstance(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                TextView weight = requireView().findViewById(R.id.RegisterWeight);
                weight.setText(String.valueOf(newVal));
            }
        });
        newFragment.show(requireActivity().getSupportFragmentManager(), String.valueOf(R.string.RetgisterWeightLabel));
    }


    private void toMainActivity(){
        Intent mainActivity = new Intent(getContext(), MainActivity.class);
        startActivity(mainActivity);
        getActivity().finish();
    }
}