package com.reactive.dc.projectreactor.app.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsuarioComentarios {

    private Usuario usuario;

    private Comentarios comentarios;

}
