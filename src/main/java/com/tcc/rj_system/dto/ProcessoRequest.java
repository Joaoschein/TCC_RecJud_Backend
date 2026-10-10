package com.tcc.rj_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.tcc.rj_system.enums.StatusProcesso;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProcessoRequest(
        @NotBlank(message = "O numero do processo e obrigatorio")
        String numeroProcesso,

        @NotBlank(message = "O nome da empresa e obrigatorio")
        @Size(max = 150, message = "O nome da empresa deve ter no maximo 150 caracteres")
        String empresa,

        @NotBlank(message = "O CNPJ da empresa e obrigatorio")
        String cnpj,

        @Size(max = 120, message = "A vara responsavel deve ter no maximo 120 caracteres")
        String vara,

        @Size(max = 150, message = "O juiz responsavel deve ter no maximo 150 caracteres")
        String juizResponsavel,

        @Size(max = 120, message = "A comarca deve ter no maximo 120 caracteres")
        String comarca,

        @Size(max = 150, message = "O administrador judicial deve ter no maximo 150 caracteres")
        String administradorJudicial,

        @NotNull(message = "A data de abertura do processo e obrigatoria")
        LocalDate abertura,

        BigDecimal valorTotal,
        @NotNull(message = "O status do processo e obrigatorio")
        StatusProcesso status,

        @Valid
        List<EventoTimelineRequest> timeline) {
}
