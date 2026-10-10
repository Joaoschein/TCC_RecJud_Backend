package com.tcc.rj_system.dto;

import java.time.LocalDate;

import com.tcc.rj_system.enums.StatusEventoTimeline;

public record EventoTimelineResponse(
        String id,
        String titulo,
        String descricao,
        LocalDate data,
        StatusEventoTimeline status) {
}
