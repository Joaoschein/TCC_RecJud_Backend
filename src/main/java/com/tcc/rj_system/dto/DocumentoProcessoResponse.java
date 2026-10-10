package com.tcc.rj_system.dto;

import java.time.Instant;

public record DocumentoProcessoResponse(
        String id,
        String processoId,
        String nome,
        String nomeArquivo,
        String contentType,
        long tamanhoBytes,
        String tipo,
        Instant dataUpload) {
}
