package com.tcc.rj_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tcc.rj_system.enums.StatusProcesso;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProcessoRequest(
        @NotBlank(message = "O numero do processo e obrigatorio")
        @Size(max = 50, message = "O numero do processo deve ter no maximo 50 caracteres")
        String numeroProcesso,

        @NotBlank(message = "O nome da empresa e obrigatorio")
        @Size(max = 150, message = "O nome da empresa deve ter no maximo 150 caracteres")
        String empresa,

        @NotBlank(message = "O CNPJ da empresa e obrigatorio")
        @Size(max = 20, message = "O CNPJ deve ter no maximo 20 caracteres")
        String cnpj,

        @NotBlank(message = "A vara responsavel e obrigatoria")
        @Size(max = 120, message = "A vara responsavel deve ter no maximo 120 caracteres")
        String vara,

        @NotNull(message = "A data de abertura do processo e obrigatoria")
        LocalDate abertura,

        @NotNull(message = "O valor total do processo e obrigatorio")
        @DecimalMin(value = "0.01", message = "O valor total deve ser maior que zero")
        BigDecimal valorTotal,

        @NotNull(message = "O status do processo e obrigatorio")
        StatusProcesso status) {
}
