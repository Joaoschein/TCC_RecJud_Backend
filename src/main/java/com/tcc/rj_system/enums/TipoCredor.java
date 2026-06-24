package com.tcc.rj_system.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TipoCredor {
    TRABALHISTA(1, "trabalhistas"),
    GARANTIA_REAL(2, "garantia real"),
    QUIROGRAFARIO(3, "quirografarios"),
    ME_EPP(4, "ME/EPP");

    private final int classe;
    private final String descricao;

    TipoCredor(int classe, String descricao) {
        this.classe = classe;
        this.descricao = descricao;
    }

    public int getClasse() {
        return classe;
    }

    public String getDescricao() {
        return descricao;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static TipoCredor from(Object value) {
        if (value instanceof Number number) {
            return fromClasse(number.intValue());
        }

        String normalized = String.valueOf(value)
                .trim()
                .replace("-", "_")
                .replace(" ", "_")
                .replace("/", "_")
                .toUpperCase();

        for (TipoCredor tipo : values()) {
            if (tipo.name().equals(normalized)
                    || tipo.descricao.replace("/", "_").replace(" ", "_").toUpperCase().equals(normalized)
                    || String.valueOf(tipo.classe).equals(normalized)) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Tipo de credor invalido: " + value);
    }

    private static TipoCredor fromClasse(int classe) {
        for (TipoCredor tipo : values()) {
            if (tipo.classe == classe) {
                return tipo;
            }
        }

        throw new IllegalArgumentException("Classe de credor invalida: " + classe);
    }
}
