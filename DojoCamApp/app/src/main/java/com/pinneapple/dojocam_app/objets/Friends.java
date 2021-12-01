package com.pinneapple.dojocam_app.objets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Friends {

    private List<String> List_friends;


    public Friends() {
        List_friends = new ArrayList<String>();
    }

    public List<String> getExercisesDone() {
        return List_friends;
    }

    public void setExercisesDone(List<String> exercisesDone) {
        this.List_friends = exercisesDone;
    }
}
