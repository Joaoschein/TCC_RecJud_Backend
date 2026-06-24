package com.tcc.rj_system.dto;

import java.math.BigDecimal;

import com.tcc.rj_system.enums.StatusCredor;
import com.tcc.rj_system.enums.TipoCredor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CredorRequest(
        @NotBlank(message = "O nome do credor e obrigatorio")
        @Size(max = 150, message = "O nome do credor deve ter no maximo 150 caracteres")
        String nome,

        @NotBlank(message = "O documento do credor e obrigatorio")
        @Size(max = 20, message = "O documento deve ter no maximo 20 caracteres")
        String documento,

        @NotBlank(message = "O id do processo e obrigatorio")
        String processoId,

        @NotNull(message = "O tipo do credor e obrigatorio")
        TipoCredor tipo,

        @NotNull(message = "O valor a receber e obrigatorio")
        @DecimalMin(value = "0.01", message = "O valor a receber deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "O status do credor e obrigatorio")
        StatusCredor status) {
}
