package com.reactive.dc.projectreactor.app.models;

import java.util.ArrayList;
import java.util.List;

public class Comentarios {

    List<String> comentariosList;

    public Comentarios(){
        this.comentariosList = new ArrayList<>();
    }

    public void addComentario(String comentario){
        this.comentariosList.add(comentario);
    }

    @Override
    public String toString() {
        return "Comentarios{" +
                "comentariosList=" + comentariosList +
                '}';
    }
}
