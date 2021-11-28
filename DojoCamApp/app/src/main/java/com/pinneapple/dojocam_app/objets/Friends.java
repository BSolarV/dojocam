package com.pinneapple.dojocam_app.objets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Friends {
    private List<String> List_friends;
    private Object Friends;

    public Friends() {
        List_friends = new ArrayList<String>();
    }

    public List<String> getFriends() {
        return List_friends;
    }

    public void setFriends(List<String> Friends_new) {
        this.Friends = Friends_new;
    }
}
