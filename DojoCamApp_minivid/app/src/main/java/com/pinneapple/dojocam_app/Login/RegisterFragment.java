package com.pinneapple.dojocam_app.Login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.pinneapple.dojocam_app.LoadingDialog;
import com.pinneapple.dojocam_app.MainActivity;
import com.pinneapple.dojocam_app.Ml_model;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.FragmentRegisterBinding;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private final LoadingDialog loadingDialog = new LoadingDialog(this);

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.RegisterSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( !binding.RegisterEmail.getText().toString().isEmpty() ) {
                    if( !binding.RegisterPassword.getText().toString().isEmpty()
                            & binding.RegisterPassword.getText().toString()
                                .equals(binding.RegisterConfirmPassword.getText().toString()) ){
                        loadingDialog.startLoadingDialog();
                        FirebaseAuth.getInstance()
                                .createUserWithEmailAndPassword(
                                        binding.RegisterEmail.getText().toString(),
                                        binding.RegisterPassword.getText().toString())
                                .addOnCompleteListener(resultTask -> {
                                    if( resultTask.isSuccessful() ){
                                        NavHostFragment.findNavController(RegisterFragment.this)
                                                .navigate(R.id.action_RegisterFragment_to_RegisterDetailsFragment);
                                        loadingDialog.dismissDialog();
                                    }
                                })
                                .addOnFailureListener(resultTask -> {
                                    loadingDialog.dismissDialog();
                                    Toast.makeText(requireContext(), resultTask.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                }
            }
        });
    }
}