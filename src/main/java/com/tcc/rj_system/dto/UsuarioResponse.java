package com.tcc.rj_system.dto;

public record UsuarioResponse(
        String id,
        String nome,
        String email,
        String role) {
}
