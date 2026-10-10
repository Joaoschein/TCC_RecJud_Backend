package com.tcc.rj_system.dto;

import java.time.LocalDate;

import com.tcc.rj_system.enums.StatusEventoTimeline;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventoTimelineRequest(
        String id,

        @NotBlank(message = "O titulo do evento e obrigatorio")
        @Size(max = 120, message = "O titulo do evento deve ter no maximo 120 caracteres")
        String titulo,

        @Size(max = 300, message = "A descricao do evento deve ter no maximo 300 caracteres")
        String descricao,

        @NotNull(message = "A data do evento e obrigatoria")
        LocalDate data,

        @NotNull(message = "O status do evento e obrigatorio")
        StatusEventoTimeline status) {
}
