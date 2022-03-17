package com.pinneapple.dojocam_app;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Newchatselector extends AppCompatActivity {

    public ArrayList<String> user_list;
    public ArrayList<String> id_list;
    public FirebaseFirestore db = FirebaseFirestore.getInstance();
    public ArrayAdapter adapter;
    public ListView l;
    ArrayAdapter<String> arr;

    public void rellenar(){
        db.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d(TAG, document.getId() + " => " + document.getData());
                                String aux = document.get("firstName", String.class) + " " + document.get("lastName", String.class) + " (" + document.getId() + ")";
                                //System.out.println(aux);
                                user_list.add(aux);
                                id_list.add(document.getId());
                                //id_list.add(aux);
                                System.out.println(user_list);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        System.out.println(user_list);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newchatselector);
        rellenar();
//        Button crear = (Button) findViewById(R.id.button33);
//        crear.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FirebaseUser user_email = FirebaseAuth.getInstance().getCurrentUser();
//                assert user_email != null;
//                String UID = user_email.getEmail();
//                String[] arr_de_email = UID.split("@", 2);
//                UID = arr_de_email[0];
//                EditText text = (EditText) findViewById(R.id.editTextTextPersonName3);
//                String uid2 = text.getText().toString();
//                String[] usuarios = {UID, uid2};
//                Log.d("das", "Login Successful : " + Arrays.toString(usuarios));
//                try {
//                    Apical.sendpost(usuarios);
//                } catch (IOException e) {
//                    Log.d("das", "error en el envioo del a callllllll");
//                    e.printStackTrace();
//                }
//            }
//        });
        System.out.println(user_list+"sadasdadasdasdadddddddddddddddddddddddddddddd");
        adapter = new ArrayAdapter(this, R.layout.list_vid, user_list );
        ListView lv = (ListView) findViewById(R.id.user_list3);
        lv.setAdapter(adapter);
    }

}