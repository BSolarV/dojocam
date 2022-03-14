package com.pinneapple.dojocam_app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


class CustomCountryList extends ArrayAdapter {
    private String[] countryNames;
    private String[] capitalNames;
    private Integer[] imageid;
    private Activity context;

    public CustomCountryList(Activity context, String[] countryNames, String[] capitalNames, Integer[] imageid) {
        super(context, R.layout.list_friends, countryNames);
        this.context = context;
        this.countryNames = countryNames;
        this.capitalNames = capitalNames;
        this.imageid = imageid;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row=convertView;
        LayoutInflater inflater = context.getLayoutInflater();
        if(convertView==null)
            row = inflater.inflate(R.layout.list_friends, null, true);
        TextView textViewCountry = (TextView) row.findViewById(R.id.textViewCountry);
        TextView textViewCapital = (TextView) row.findViewById(R.id.textViewCapital);
        ImageView imageFlag = (ImageView) row.findViewById(R.id.imageViewFlag);

        textViewCountry.setText(countryNames[position]);
        textViewCapital.setText(capitalNames[position]);
        imageFlag.setImageResource(imageid[position]);
        return  row;
    }
}