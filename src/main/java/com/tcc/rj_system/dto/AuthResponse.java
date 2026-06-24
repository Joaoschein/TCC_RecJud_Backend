package com.tcc.rj_system.dto;

public record AuthResponse(
        String tokenType,
        String accessToken,
        Long expiresIn,
        UsuarioResponse usuario) {
}
