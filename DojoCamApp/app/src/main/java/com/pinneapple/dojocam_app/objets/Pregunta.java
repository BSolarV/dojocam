package com.pinneapple.dojocam_app.objets;

import java.util.List;

public class Pregunta {
    private String id;
    private String pregunta;
    private String respuesta;
    private List<String> rating;
    private List<String> ratingNeg;
    private Integer interes;
    public Pregunta() {

    }

    public Pregunta(String id, String pregunta, String respuesta,List<String> rating,List<String> ratingNeg,Integer interes) {
        this.id = id;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.rating = rating;
        this.ratingNeg = ratingNeg;
        this.interes = interes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public List<String> getRating() {
        return rating;
    }

    public void setRating(List<String> rating) {
        this.rating = rating;
    }

    public List<String> getRatingNeg() {
        return ratingNeg;
    }

    public void setRatingNeg(List<String> ratingNeg) {
        this.ratingNeg = ratingNeg;
    }

    public Integer getInteres() {
        return interes;
    }

    public void setInteres(Integer interes) {
        this.interes = interes;
    }
}
