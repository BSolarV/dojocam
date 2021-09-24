package com.pinneapple.dojocam_app.Login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.pinneapple.dojocam_app.MainActivity;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.LoginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( !binding.LoginEmail.getText().toString().isEmpty() &
                        !binding.LoginPassword.getText().toString().isEmpty() ) {
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(
                            binding.LoginEmail.getText().toString(),
                            binding.LoginPassword.getText().toString())
                        .addOnCompleteListener(resultTask -> {
                            if( resultTask.isSuccessful() ){
                                Intent mainActivity = new Intent(getContext(), MainActivity.class);
                                startActivity(mainActivity);
                                getActivity().finish();
                            }
                        });
                    }
                }
            });

        binding.LoginRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_LoginFragment_to_RegisterFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}