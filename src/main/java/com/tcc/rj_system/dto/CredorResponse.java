package com.tcc.rj_system.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.tcc.rj_system.enums.StatusCredor;
import com.tcc.rj_system.enums.TipoCredor;

public record CredorResponse(
        String id,
        String nome,
        String documento,
        String processoId,
        String numeroProcesso,
        String processoNome,
        TipoCredor tipo,
        int classe,
        String tipoDescricao,
        BigDecimal valor,
        StatusCredor status,
        Instant dataCadastro) {
}
