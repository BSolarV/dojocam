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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.pinneapple.dojocam_app.MainActivity;
//import com.pinneapple.dojocam_app.Ml_model;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.dialogs.DatePickerFragment;

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
public class RegisterDetailsFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    // Attributes
    private EditText name;
    private EditText lastname;
    private Spinner sex;
    private TextView birthdate;
    private Date birthDateValue;
    private TextView height;
    private TextView weight;

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



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_details, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = new GregorianCalendar(year, month, dayOfMonth);
        DateFormat formatter = new SimpleDateFormat("dd/mm/yyyy");
        birthdate.setText( formatter.format(calendar.getTime()) );
        birthDateValue = calendar.getTime();
    }

    private void toMainActivity(){
        Intent mainActivity = new Intent(getContext(), MainActivity.class);
        startActivity(mainActivity);
        getActivity().finish();
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(requireActivity().getSupportFragmentManager(), String.valueOf(R.string.RegisterBirthDateLabel));
        birthDateValue = newFragment.getBirthDateValue();
    }

    public void showHeightPickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(requireActivity().getSupportFragmentManager(), String.valueOf(R.string.RegisterBirthDateLabel));
        birthDateValue = newFragment.getBirthDateValue();
    }

}