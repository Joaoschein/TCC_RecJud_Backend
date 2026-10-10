package com.tcc.rj_system.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
@Document(collection = "documentos_processos")
public class DocumentoProcesso {

    @Id
    private String id;

    @Indexed
    private String processoId;

    private String nome;

    private String nomeArquivo;

    private String contentType;

    private long tamanhoBytes;

    private String tipo;

    private String caminhoArquivo;

    private Instant dataUpload;
}
