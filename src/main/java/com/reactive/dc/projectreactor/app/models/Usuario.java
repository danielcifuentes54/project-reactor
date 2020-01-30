package com.reactive.dc.projectreactor.app.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Usuario {

    private String nombre;

    private String apellido;
}
