package com.tcc.rj_system.enums;

import java.text.Normalizer;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum StatusProcesso {
    EM_ANDAMENTO,
    AGUARDANDO_APROVACAO,
    APROVADO;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StatusProcesso from(String value) {
        String normalized = Normalizer.normalize(String.valueOf(value).trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_|_$", "");

        if ("INICIAL".equals(normalized)) {
            return EM_ANDAMENTO;
        }

        for (StatusProcesso status : values()) {
            if (status.name().equals(normalized)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Status do processo invalido: " + value);
    }
}
