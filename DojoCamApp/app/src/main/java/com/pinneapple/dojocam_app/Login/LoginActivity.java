package com.pinneapple.dojocam_app.Login;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pinneapple.dojocam_app.MainActivity_ml;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        checkAuth();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTheme(R.style.Theme_DojoCamApp_NoActionBar);
    }

    private void checkAuth(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if( user != null ){
            Intent mainActivity = new Intent(this, MainActivity_ml.class);
            startActivity(mainActivity);
            finish();
        }
    }
}