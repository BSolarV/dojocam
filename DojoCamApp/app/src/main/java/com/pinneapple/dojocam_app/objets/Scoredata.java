package com.pinneapple.dojocam_app.objets;

import java.util.HashMap;
import java.util.List;

public class Scoredata {

    private String id;
    private String name;
    private HashMap<String, HashMap<String, List<Integer>>> scores;

    public Scoredata() {
    }

    public Scoredata(String id, String name, HashMap<String, HashMap<String, List<Integer>>> scores) {
        this.id = id;
        this.name = name;
        this.scores = scores;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, HashMap<String, List<Integer>>> getScores() {
        return scores;
    }

    public void setScores(HashMap<String, HashMap<String, List<Integer>>> scores) {
        this.scores = scores;
    }
}
