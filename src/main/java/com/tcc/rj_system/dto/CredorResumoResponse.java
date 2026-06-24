package com.tcc.rj_system.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.tcc.rj_system.enums.StatusCredor;
import com.tcc.rj_system.enums.TipoCredor;

public record CredorResumoResponse(
        String id,
        String nome,
        String documento,
        TipoCredor tipo,
        int classe,
        String tipoDescricao,
        BigDecimal valor,
        StatusCredor status,
        Instant dataCadastro) {
}
