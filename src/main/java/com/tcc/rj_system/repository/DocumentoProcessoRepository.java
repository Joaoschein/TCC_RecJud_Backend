package com.tcc.rj_system.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tcc.rj_system.model.DocumentoProcesso;

public interface DocumentoProcessoRepository extends MongoRepository<DocumentoProcesso, String> {

    List<DocumentoProcesso> findByProcessoIdOrderByDataUploadDesc(String processoId);

    void deleteByProcessoId(String processoId);
}
