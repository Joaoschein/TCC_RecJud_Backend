package com.tcc.rj_system.model;

import java.time.LocalDate;

import com.tcc.rj_system.enums.StatusEventoTimeline;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoTimeline {

    private String id;

    private String titulo;

    private String descricao;

    private LocalDate data;

    private StatusEventoTimeline status;
}
