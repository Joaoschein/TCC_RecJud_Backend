package com.tcc.rj_system.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CredorImportRequest(
        @NotEmpty(message = "Informe ao menos um credor para importar")
        List<@NotNull @Valid CredorRequest> credores) {
}
