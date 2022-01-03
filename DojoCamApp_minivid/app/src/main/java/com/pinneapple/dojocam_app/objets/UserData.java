package com.pinneapple.dojocam_app.objets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserData {
    private String firstName;
    private String lastName;
    private int sex;
    private Date birthDate;
    private Integer height;
    private Integer weight;
    private Integer score;
    private List<String> exercisesDone;

    public UserData(){}

    public UserData(String firstName, String lastName, int sex, Date birthDate, Integer height, Integer weight) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        score = 0;
        exercisesDone = new ArrayList<String>();
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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public List<String> getExercisesDone() {
        return exercisesDone;
    }

    public void setExercisesDone(List<String> exercisesDone) {
        this.exercisesDone = exercisesDone;
    }
}
