package com.pinneapple.dojocam_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.cometchat.pro.core.AppSettings;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.GroupsRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Group;
import com.cometchat.pro.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class GroupList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        initCometChat();
        getGroupList();
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
        FirebaseUser user_email = FirebaseAuth.getInstance().getCurrentUser();
        assert user_email != null;
        String UID = user_email.getEmail(); // Replace with the UID of the user to login
        AppSettings appSettings=new AppSettings.AppSettingsBuilder().subscribePresenceForAllUsers().setRegion(region).build();

        CometChat.init(this, appID,appSettings, new CometChat.CallbackListener<String>() {
            @Override
            public void onSuccess(String successMessage) {
                if (CometChat.getLoggedInUser() == null) {

                    CometChat.login(UID, authKey, new CometChat.CallbackListener<User>() {
                        @Override
                        public void onSuccess(User user) {
                            Log.d("das", "Login Successful : " + user.toString());
                        }

                        @Override
                        public void onError(CometChatException e) {
                            Log.d("error", "Login failed with exception: " + e.getMessage());
                        }
                    });
                }

            }
            @Override
            public void onError(CometChatException e) {
            }
        });
    }
}