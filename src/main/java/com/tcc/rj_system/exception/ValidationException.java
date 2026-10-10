package com.tcc.rj_system.exception;

import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super("Dados de entrada invalidos");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
