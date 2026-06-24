package com.tcc.rj_system.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tcc.rj_system.model.Processo;

public interface ProcessoRepository extends MongoRepository<Processo, String> {
}
