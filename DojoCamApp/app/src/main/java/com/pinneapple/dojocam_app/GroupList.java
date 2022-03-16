package com.pinneapple.dojocam_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.cometchat.pro.core.AppSettings;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.GroupsRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GroupList extends AppCompatActivity {
    FirebaseUser user_email = FirebaseAuth.getInstance().getCurrentUser();
    String UID = user_email.getEmail(); // Replace with the UID of the user to login
    String[] arr_de_email = UID.split("@", 2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = this;
        setContentView(R.layout.activity_group_list);
        activity.setTitle("Settings");
        initCometChat();
        getGroupList();
        ImageButton newchat = (ImageButton) findViewById(R.id.imageButtonchat);
        newchat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Newchatselector.class);
                startActivity(intent);
            }
        });
    }

    private void getGroupList() {
        GroupsRequest groupsRequest = new GroupsRequest.GroupsRequestBuilder().build();
        groupsRequest.fetchNext(new CometChat.CallbackListener<List<Group>>() {
            @Override
            public void onSuccess(List <Group> list) {
                updateUI(list);
            }
            @Override
            public void onError(CometChatException e) {
            }
        });
    }

    private void updateUI(List<Group> list) {
        RecyclerView groupsRecycleView = findViewById(R.id.groupsRecycleView);
        groupsRecycleView.setLayoutManager(new LinearLayoutManager(this));
        ChatAdapter groupsAdapter = new ChatAdapter(list, this);
        groupsRecycleView.setAdapter(groupsAdapter);
    }

    public void initCometChat(){
        String appID = "1985642356a8baff"; // Replace with your App ID
        String region = "us"; // Replace with your App Region ("eu" or "us")
        String authKey = "8d0188e55822c22f0d2f20cdcd8601c694b6780f"; //Replace with your Auth Key.
        String[] arr_de_email_usuario = {};
        String[] arr_de_emails_super = {"superhero1", "superhero2", "superhero3", "superhero5"};
        Random rand = new Random();
        AppSettings appSettings=new AppSettings.AppSettingsBuilder().subscribePresenceForAllUsers().setRegion(region).build();
        CometChat.init(this, appID,appSettings, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successMessage) {
                if (CometChat.getLoggedInUser() == null) {
                    LoginChat isLogged = new LoginChat();
                    LoginChat.isLogged = true;
                    CometChat.login(arr_de_email[0], authKey, new CometChat.CallbackListener<User>() {
                        @Override
                        public void onSuccess(User user) {
                            Log.d("das", "Login Successful ------------------------------------------------------------: " + user.toString());
                        }

                        @Override
                        public void onError(CometChatException e) {
                            Log.d("error", "Login failed with exception: " + e.getMessage());
                            try {
                                Log.d("error", "Se CREOOO EL USUARIO");
                                Apical.crearusuario(arr_de_email[0]);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                }
            }
            @Override
            public void onError(CometChatException e) {
            }
        });
    }
//    @Override
//    public void onPause()
//    {
//        super.onPause();
//        if(isFinishing())
//        {
//            CometChat.logout(new CometChat.CallbackListener<String>() {
//                @Override
//                public void onSuccess(String successMessage) {
//                    Log.d("asd", "Logout completed successfully");
//                }
//                @Override
//                public void onError(CometChatException e) {
//                    Log.d("TAG", "Logout failed with exception: " + e.getMessage());
//                }
//            });
//        }
//    }
}