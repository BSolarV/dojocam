package com.pinneapple.dojocam_app.objets;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserData {

    private String firstName;
    private String lastName;
    private int sex;
    private Date birthDate;
    private Integer height;
    private Integer weight;
    //old {ID_ejercicio: {Fecha: Score}}
    //new {ID_ejercicio: {Fecha: [Score1, Score2, Score3, ...]}}
    //private HashMap<String, HashMap<String, HashMap<String, List<Integer>>>> scores;
    //private List<String> exercisesDone;
    private String lastExercise;
    private String lastExercisePath;

    public UserData(){}

    public UserData(String firstName, String lastName, int sex, Date birthDate, Integer height, Integer weight, String lastExercise, String lastExercisePath) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.lastExercise = lastExercise;
        this.lastExercisePath = lastExercisePath;
        //score = null;
        //exercisesDone = new ArrayList<String>();
    }
    public UserData(String firstName, String lastName, int sex, Date birthDate, Integer height, Integer weight) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.lastExercise = "basico";
        this.lastExercisePath = "";
        //score = null;
        //exercisesDone = new ArrayList<String>();
    }
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    /*
    public List<String> getExercisesDone() {
        return exercisesDone;
    }

    public void setExercisesDone(List<String> exercisesDone) {
        this.exercisesDone = exercisesDone;
    }


    public HashMap<String, HashMap<String, HashMap<String, List<Integer>>>> getScores() {

        return scores;
    }

    public void setScores(HashMap<String, HashMap<String, HashMap<String, List<Integer>>>> scores) {
        this.scores = scores;
    }
    */

    public String getLastExercise() {
        return lastExercise;
    }
    public void setLastExercise(String lastExercise){
        this.lastExercise = lastExercise;
    }

    public String getLastExercisePath() {
        return lastExercisePath;
    }
    public void setLastExercisePath(String lastExercisePath){ this.lastExercisePath = lastExercisePath; }
}
