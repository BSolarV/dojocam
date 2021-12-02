package com.pinneapple.dojocam_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.GroupsRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.Group;

import java.util.List;

public class GroupList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        
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
}