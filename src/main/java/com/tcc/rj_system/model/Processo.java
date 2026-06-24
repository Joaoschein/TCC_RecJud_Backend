package com.tcc.rj_system.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.tcc.rj_system.enums.StatusProcesso;

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
@Document(collection = "processos")
public class Processo {

    @Id
    private String id;

    private String numeroProcesso;

    private String empresa;

    private String cnpj;

    private String vara;

    private LocalDate abertura;

    private BigDecimal valorTotal;

    private StatusProcesso status;

    @Builder.Default
    @DBRef
    private List<Credor> credores = new ArrayList<>();
}
