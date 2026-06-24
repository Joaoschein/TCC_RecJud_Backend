package com.tcc.rj_system.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum StatusCredor {
    HABILITADO,
    PENDENTE,
    IMPUGNADO;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StatusCredor from(String value) {
        String normalized = String.valueOf(value).trim().replace(" ", "_").toUpperCase();

        for (StatusCredor status : values()) {
            if (status.name().equals(normalized)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Status do credor invalido: " + value);
    }
}
