package com.pinneapple.dojocam_app.Login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.pinneapple.dojocam_app.LoadingDialog;
import com.pinneapple.dojocam_app.MainActivity;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {


    private FragmentLoginBinding binding;
    private final LoadingDialog loadingDialog = new LoadingDialog(this);

    private String email;

    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);


        oneTapClient = Identity.getSignInClient(requireActivity());
        signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.AppClientId))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(true)
                        .build())
                // Automatically sign in when exactly one credential is retrieved.
                .setAutoSelectEnabled(true)
                .build();



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

        binding.GoogleSingIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener(requireActivity(), new OnSuccessListener<BeginSignInResult>() {
                            @Override
                            public void onSuccess(BeginSignInResult result) {
                                try {
                                    startIntentSenderForResult(
                                            result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                                            null, 0, 0, 0, null);
                                } catch (IntentSender.SendIntentException e) {
                                    Toast.makeText(requireContext(), "Couldn't start One Tap UI: " + e.getLocalizedMessage(), Toast.LENGTH_LONG);
                                }
                            }
                        })
                        .addOnFailureListener(requireActivity(), new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // No saved credentials found. Launch the One Tap sign-up flow, or
                                // do nothing and continue presenting the signed-out UI.
                                Toast.makeText(requireContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG);
                            }
                        });
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadingDialog.startLoadingDialog();
        switch (requestCode) {
            case REQ_ONE_TAP:
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                    String idToken = credential.getGoogleIdToken();
                    String username = credential.getId();
                    String password = credential.getPassword();
                    if (idToken !=  null) {
                        // Got an ID token from Google. Use it to authenticate
                        // with your backend.
                        AuthCredential FBcredential = GoogleAuthProvider.getCredential(idToken, null);
                        FirebaseAuth.getInstance().signInWithCredential(FBcredential)
                                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Intent mainActivity = new Intent(getContext(), MainActivity.class);
                                            startActivity(mainActivity);
                                            loadingDialog.dismissDialog();
                                            requireActivity().finish();
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Toast.makeText(requireContext(), task.getException().getMessage(), Toast.LENGTH_LONG);
                                        }
                                    }
                                });

                    } else if ( password != null ) {
                        // Got a saved username and password. Use them to authenticate
                        // with your backend.
                        FirebaseAuth.getInstance()
                                .signInWithEmailAndPassword(
                                        username,
                                        password)
                                .addOnCompleteListener(resultTask -> {
                                    if( resultTask.isSuccessful() ){
                                        Intent mainActivity = new Intent(getContext(), MainActivity.class);
                                        startActivity(mainActivity);
                                        loadingDialog.dismissDialog();
                                        requireActivity().finish();
                                    }
                                })
                                .addOnFailureListener(resultTask -> {
                                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                                            username,
                                            password)
                                            .addOnCompleteListener(resultTaskRegister -> {
                                                if( resultTaskRegister.isSuccessful() ){
                                                    NavHostFragment.findNavController(LoginFragment.this)
                                                            .navigate(R.id.action_LoginFragment_to_RegisterDetailsFragment);
                                                    loadingDialog.dismissDialog();
                                                }
                                            })
                                            .addOnFailureListener(resultTaskRegister -> {
                                                loadingDialog.dismissDialog();
                                                Toast.makeText(requireContext(), resultTaskRegister.getMessage(), Toast.LENGTH_LONG).show();
                                            });
                                });
                    }
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case CommonStatusCodes.CANCELED:
                            showOneTapUI = false;
                            break;
                        case CommonStatusCodes.NETWORK_ERROR:
                            Toast.makeText(requireContext(), R.string.ConectionError, Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(requireContext(), R.string.NoUser, Toast.LENGTH_LONG);
                            break;
                    }
                    if (loadingDialog.getStatus()) loadingDialog.dismissDialog();
                }
                break;
        }
    }
}