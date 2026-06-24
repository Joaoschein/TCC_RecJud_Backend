package com.tcc.rj_system.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.tcc.rj_system.enums.StatusCredor;
import com.tcc.rj_system.enums.TipoCredor;

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
@Document(collection = "credores")
public class Credor {

    @Id
    private String id;

    private String nome;

    private String documento;

    @DBRef
    private Processo processo;

    private TipoCredor tipo;

    private BigDecimal valor;

    private StatusCredor status;

    private Instant dataCadastro;
}
