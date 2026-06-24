package com.tcc.rj_system.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tcc.rj_system.dto.ProcessoRequest;
import com.tcc.rj_system.dto.ProcessoResponse;
import com.tcc.rj_system.service.ProcessoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/processos")
@RequiredArgsConstructor
public class ProcessoController {

    private final ProcessoService processoService;

    @PostMapping
    public ResponseEntity<ProcessoResponse> criar(@Valid @RequestBody ProcessoRequest request) {
        ProcessoResponse response = processoService.criar(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProcessoResponse>> listar() {
        return ResponseEntity.ok(processoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessoResponse> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(processoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcessoResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody ProcessoRequest request) {
        return ResponseEntity.ok(processoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable String id) {
        processoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
