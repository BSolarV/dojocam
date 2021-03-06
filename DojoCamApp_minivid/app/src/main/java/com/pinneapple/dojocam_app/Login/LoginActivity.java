package com.pinneapple.dojocam_app.Login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pinneapple.dojocam_app.MainActivity;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.ActivityLoginBinding;

import org.jetbrains.annotations.NotNull;

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
            Intent mainActivity = new Intent(this, MainActivity.class);
            startActivity(mainActivity);
            finish();
        }
    }


}