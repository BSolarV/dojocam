package com.pinneapple.dojocam_app.objects;



public class VideoInfo {
    private String id;
    private String nombre;
    private String descripcion;
    private String dificultad;
    private String vid_path;

    public VideoInfo() {

    }

    public VideoInfo(String id, String nombre, String descripcion, String dificultad, String vid_path) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.dificultad = dificultad;
        this.vid_path = vid_path;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDificultad() {
        return dificultad;
    }

    public void setDificultad(String dificultad) {
        this.dificultad = dificultad;
    }

    public String getVid_path() {
        return vid_path;
    }

    public void setVid_path(String vid_path) {
        this.vid_path = vid_path;
    }
}


