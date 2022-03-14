package com.pinneapple.dojocam_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.Arrays;

public class Newchatselector extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newchatselector);


        Button crear = (Button) findViewById(R.id.button33);
        crear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user_email = FirebaseAuth.getInstance().getCurrentUser();
                assert user_email != null;
                String UID = user_email.getEmail();
                String[] arr_de_email = UID.split("@", 2);
                UID = arr_de_email[0];
                EditText text = (EditText) findViewById(R.id.editTextTextPersonName3);
                String uid2 = text.getText().toString();
                String[] usuarios = {UID, uid2};
                Log.d("das", "Login Successful : " + Arrays.toString(usuarios));
                try {
                    Apical.main(usuarios);
                } catch (IOException e) {
                    Log.d("das", "error en el envioo del a callllllll");
                    e.printStackTrace();
                }
            }
        });
    }
}