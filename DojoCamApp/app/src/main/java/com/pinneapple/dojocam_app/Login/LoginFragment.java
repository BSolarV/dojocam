package com.pinneapple.dojocam_app.Login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pinneapple.dojocam_app.LoadingDialog;
import com.pinneapple.dojocam_app.MainActivity;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.FragmentLoginBinding;

import java.util.List;
import java.util.Objects;

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
            @NonNull LayoutInflater inflater, ViewGroup container,
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
                        // Show all accounts on the device.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                // Automatically sign in when exactly one credential is retrieved.
                //.setAutoSelectEnabled(true)
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
                    Log.wtf("LoginFragment", "Login");
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(
                            binding.LoginEmail.getText().toString(),
                            binding.LoginPassword.getText().toString())
                            .addOnCompleteListener(onActivityResult -> {
                                Log.wtf("LoginFragment", onActivityResult.getResult().toString());
                            })
                        .addOnCompleteListener(resultTask -> {
                            Log.wtf("LoginFragment", "Login Success");
                            Log.wtf("LoginFragment", resultTask.getResult().toString());
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
                loadingDialog.startLoadingDialog();
                if( showOneTapUI ){
                    showOneTapUI = false;
                    oneTapClient.beginSignIn(signInRequest)
                            .addOnCompleteListener(task -> {
                                loadingDialog.dismissDialog();
                            })
                            .addOnSuccessListener(requireActivity(), result -> {
                                try {
                                    startIntentSenderForResult(
                                            result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                                            null, 0, 0, 0, null);
                                } catch (IntentSender.SendIntentException e) {
                                    Toast.makeText(requireContext(), "No se pudo conectar con servicios de Google.", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(requireActivity(), new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // No saved credentials found. Launch the One Tap sign-up flow, or
                                    // do nothing and continue presenting the signed-out UI.
                                    Toast.makeText(requireContext(), "Ocurrió un error inesperado con los servicios de Google.", Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    loadingDialog.dismissDialog();
                    Toast.makeText(requireContext(), "Por motivos de seguridad reinicie la aplicación para reintentar con Google.", Toast.LENGTH_LONG).show();
                }
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
                        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(username)
                        .addOnSuccessListener(command -> {
                            if( Objects.requireNonNull(command.getSignInMethods()).isEmpty() ){
                                Bundle bundle = new Bundle();
                                bundle.putBoolean(RegisterDetailsFragment.GSINGIN, true);
                                bundle.putParcelable(RegisterDetailsFragment.GCREDENTIAL, FBcredential);
                                bundle.putString(RegisterDetailsFragment.EMAIL, username);// Serializable Object
                                NavHostFragment.findNavController(this)
                                        .navigate(R.id.action_LoginFragment_to_RegisterDetailsFragment, bundle);
                                loadingDialog.dismissDialog();
                            } else {
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
                                                    Toast.makeText(requireContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                                    showOneTapUI = false;
                                                    loadingDialog.dismissDialog();
                                                }
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(command -> {
                            Toast.makeText(requireContext(), command.getMessage(), Toast.LENGTH_LONG).show();
                            loadingDialog.dismissDialog();
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
                                    }else{
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(requireContext(), Objects.requireNonNull(resultTask.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(resultTask -> {
                                    FirebaseAuth.getInstance()
                                            .fetchSignInMethodsForEmail ( username )
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    SignInMethodQueryResult result = task.getResult();
                                                    List<String> signInMethods = result.getSignInMethods();
                                                    assert signInMethods != null;
                                                    if( signInMethods.isEmpty() ){
                                                        Bundle bundle = new Bundle();
                                                        bundle.putString(RegisterDetailsFragment.EMAIL, username);
                                                        bundle.putString(RegisterDetailsFragment.PASSWORD, password);
                                                        NavHostFragment.findNavController(this)
                                                                .navigate(R.id.action_LoginFragment_to_RegisterDetailsFragment, bundle);
                                                        loadingDialog.dismissDialog();
                                                    }
                                                } else {
                                                    Toast.makeText(requireContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            })
                                            .addOnFailureListener(task -> {
                                                Toast.makeText(requireContext(), task.getMessage(), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(requireContext(), R.string.NoUser, Toast.LENGTH_LONG).show();
                            break;
                    }
                    if (loadingDialog.getStatus()) loadingDialog.dismissDialog();
                }
                break;
        }
    }
}