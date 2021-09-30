package com.pinneapple.dojocam_app.Login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.pinneapple.dojocam_app.LoadingDialog;
import com.pinneapple.dojocam_app.MainActivity;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {


    private FragmentLoginBinding binding;
    private final LoadingDialog loadingDialog = new LoadingDialog(this);

    private String email;


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
                    loadingDialog.startLoadingDialog();
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(
                            binding.LoginEmail.getText().toString(),
                            binding.LoginPassword.getText().toString())
                        .addOnCompleteListener(resultTask -> {
                            if( resultTask.isSuccessful() ){
                                Intent mainActivity = new Intent(getContext(), MainActivity.class);
                                startActivity(mainActivity);
                                loadingDialog.dismissDialog();
                                requireActivity().finish();
                            }
                        })
                    .addOnFailureListener(resultTask -> {
                        loadingDialog.dismissDialog();
                        Toast.makeText(requireContext(), resultTask.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });

        binding.LoginForgotten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEmailDialog();
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

    private void createEmailDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.LoginEmailRequest);

        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Set up the input
        final EditText input = new EditText(requireContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.LoginEmailRequestHint);
        input.setPadding(25,25,25,25);
        linearLayout.setPadding(25,25,25,25);
        linearLayout.addView(input, inputParams);
        builder.setView(linearLayout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                email = input.getText().toString();
                FirebaseAuth.getInstance().sendPasswordResetEmail(email);
                createNotifyDone();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void createNotifyDone(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setPadding(25,25,25,25);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Set up the input
        final TextView text = new TextView(requireContext());
        text.setPadding(25,25,25,25);
        text.setText(R.string.LoginEmailNotify);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        linearLayout.addView(text, textParams);
        builder.setView(linearLayout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}