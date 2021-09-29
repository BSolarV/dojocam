package com.pinneapple.dojocam_app.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.pinneapple.dojocam_app.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private Date birthDateValue;

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
        TextView birthdate = requireView().findViewById( R.id.RegisterBirthDate );
        Calendar calendar = new GregorianCalendar(year, month, dayOfMonth);
        DateFormat formatter = new SimpleDateFormat("dd/mm/yyyy");
        birthdate.setText( formatter.format(calendar.getTime()) );
        birthDateValue = calendar.getTime();
    }

    public Date getBirthDateValue() {
        return birthDateValue;
    }
}