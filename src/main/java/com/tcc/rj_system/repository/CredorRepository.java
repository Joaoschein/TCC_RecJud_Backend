package com.tcc.rj_system.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tcc.rj_system.model.Credor;
import com.tcc.rj_system.model.Processo;

public interface CredorRepository extends MongoRepository<Credor, String> {

    List<Credor> findByProcesso(Processo processo);
    boolean existsByProcesso(Processo processo);
}
