package com.pinneapple.dojocam_app.objets;

import java.util.ArrayList;

public class Friends {

    private ArrayList<String> List_friends;

    public Friends() {
        List_friends = new ArrayList<String>();
    }

    public ArrayList<String> getFollowers() {
        return List_friends;
    }

    public void setFollowers(ArrayList<String> exercisesDone) {
        this.List_friends = exercisesDone;
    }
    public void add(String exercisesDone2) {
        List_friends.add(exercisesDone2);
    }
    public boolean contains(String exercisesDone2) {
        boolean existe = List_friends.contains(exercisesDone2);
        return existe;
    }
}
