package com.pinneapple.dojocam_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.cometchat.pro.models.Group;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.GroupViewHolder> {

    private List<Group> groups;
    private Context context;

    public ChatAdapter(List<Group> groups, Context context){
        this.groups = groups;
        this.context = context;
    }


    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupViewHolder (LayoutInflater.from(parent.getContext()).inflate(R.layout.group_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.bind(groups.get(position));

    }
    @Override
    public int getItemCount() {
        return groups.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupNameTextView;
        LinearLayout containerLayout;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.groupsNameTextView);
            containerLayout = itemView.findViewById(R.id.containerLayout);
        }

        public void bind(Group group){
            groupNameTextView.setText(group.getName());
            containerLayout.setOnClickListener(view -> ChatActivity.start(context, group.getGuid()));
        }
    }
}
