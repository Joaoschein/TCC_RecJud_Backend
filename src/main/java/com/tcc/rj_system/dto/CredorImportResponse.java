package com.tcc.rj_system.dto;

import java.util.List;

public record CredorImportResponse(
        int totalImportados,
        List<CredorResponse> credores) {
}
