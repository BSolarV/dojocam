package com.pinneapple.dojocam_app.Login;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pinneapple.dojocam_app.LoadingDialog;
import com.pinneapple.dojocam_app.MainActivity;
import com.pinneapple.dojocam_app.Ml_model;
import com.pinneapple.dojocam_app.R;
import com.pinneapple.dojocam_app.databinding.FragmentRegisterBinding;
import com.pinneapple.dojocam_app.objets.Friends;

import java.util.List;
import java.util.Objects;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private final LoadingDialog loadingDialog = new LoadingDialog(this);

    private Integer option;
    private Boolean error;
    private String message;

    private TextView emailMessage;
    private EditText emailContainer;

    private TextView passMessage;
    private EditText passContainer;

    private TextView confirmPassMessage;
    private EditText confirmPassContainer;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);

        emailMessage = binding.RegisterEmailMessage;
        emailContainer = binding.RegisterEmail;
        passMessage = binding.RegisterPasswordMessage;
        passContainer   = binding.RegisterPassword;
        confirmPassMessage  = binding.RegisterConfirmPasswordMessage;
        confirmPassContainer    = binding.RegisterConfirmPassword;

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.RegisterSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailContainer.getText().toString();
                String pass = passContainer.getText().toString();
                String confirmPass = confirmPassContainer.getText().toString();

                if( !email.isEmpty() ) {
                    if( !pass.isEmpty() & pass.length() > 5 ) {
                        if (pass.equals(confirmPass)) {
                            loadingDialog.startLoadingDialog();
                            FirebaseAuth.getInstance()
                                    .fetchSignInMethodsForEmail ( email )
                                    .addOnCompleteListener(resultTask -> {
                                        if (resultTask.isSuccessful()) {
                                            SignInMethodQueryResult result = resultTask.getResult();
                                            List<String> signInMethods = result.getSignInMethods();
                                            if( signInMethods.isEmpty() ){
                                                Bundle bundle = new Bundle();
                                                bundle.putString(RegisterDetailsFragment.EMAIL, email);
                                                bundle.putString(RegisterDetailsFragment.PASSWORD, pass); // Serializable Object

                                                System.out.println("Hola aqui nos vamos\n");
                                                System.out.println(email);

                                                Friends followers = new Friends();
                                                followers.add("basty@jeje.com");
                                                System.out.println("COmo SHoro\n");

                                                System.out.println(email);


                                                db.collection("Friends").document(email)
                                                        .set(followers)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "Guardado la lista de amigos vacia");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w(TAG, "No se logro Guardar", e);
                                                            }
                                                        });
                                                System.out.println("Hola aqui nos vamos");

                                                NavHostFragment.findNavController(RegisterFragment.this)
                                                        .navigate(R.id.action_RegisterFragment_to_RegisterDetailsFragment, bundle);
                                                loadingDialog.dismissDialog();
                                            } else {
                                                option = 0;
                                                error = true;
                                                message = getResources().getString(R.string.emailInUse);
                                                updateFieldMessage();
                                                loadingDialog.dismissDialog();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(resultTask -> {
                                        option = 0;
                                        error = true;
                                        message = resultTask.getMessage();
                                        updateFieldMessage();
                                    });
                        } else {
                            option = 2;
                            error = true;
                            message = getResources().getString(R.string.passDontMatch);
                            updateFieldMessage();
                        }
                    } else {
                        option = 1;
                        error = true;
                        message = getResources().getString(R.string.weekPass);
                        updateFieldMessage();
                    }
                } else {
                    option = 0;
                    error = true;
                    message = getResources().getString(R.string.requiredEmail);
                    updateFieldMessage();
                }
            }
        });
    }
    private void updateFieldMessage(){
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                option = -1;
                error = false;
                message = "";
                updateFieldMessage();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        switch (option){
            case -1:
                emailMessage.setText("");
                emailContainer.setTextColor(getResources().getColor(R.color.textOnMainBg, requireActivity().getTheme()));
                emailContainer.removeTextChangedListener(watcher);
                passMessage.setText("");
                passContainer.setTextColor(getResources().getColor(R.color.textOnMainBg, requireActivity().getTheme()));
                passContainer.removeTextChangedListener(watcher);
                confirmPassMessage.setText("");
                confirmPassContainer.setTextColor(getResources().getColor(R.color.textOnMainBg, requireActivity().getTheme()));
                confirmPassContainer.removeTextChangedListener(watcher);
                break;
            case 0:
                emailMessage.setText(message);
                emailContainer.setTextColor(getResources().getColor(R.color.danger, requireActivity().getTheme()));
                emailContainer.addTextChangedListener(watcher);
                break;
            case 1:
                passMessage.setText(message);
                passContainer.setTextColor(getResources().getColor(R.color.danger, requireActivity().getTheme()));
                passContainer.addTextChangedListener(watcher);
                break;
            case 2:
                passMessage.setText("");
                passContainer.setTextColor(getResources().getColor(R.color.danger, requireActivity().getTheme()));
                confirmPassMessage.setText(message);
                confirmPassContainer.setTextColor(getResources().getColor(R.color.danger, requireActivity().getTheme()));
                confirmPassContainer.addTextChangedListener(watcher);
                break;
        }
    }
}