package com.tcc.rj_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.tcc.rj_system.enums.StatusProcesso;

public record ProcessoResponse(
        String id,
        String numeroProcesso,
        String empresa,
        String cnpj,
        String vara,
        LocalDate abertura,
        BigDecimal valorTotal,
        StatusProcesso status,
        List<CredorResumoResponse> credores) {
}
