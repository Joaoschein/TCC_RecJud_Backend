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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tcc.rj_system.dto.CredorRequest;
import com.tcc.rj_system.dto.CredorResponse;
import com.tcc.rj_system.service.CredorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/credores")
@RequiredArgsConstructor
public class CredorController {

    private final CredorService credorService;

    @PostMapping
    public ResponseEntity<CredorResponse> criar(@Valid @RequestBody CredorRequest request) {
        CredorResponse response = credorService.criar(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CredorResponse>> listar(@RequestParam(required = false) String processoId) {
        if (processoId != null && !processoId.isBlank()) {
            return ResponseEntity.ok(credorService.listarPorProcesso(processoId));
        }

        return ResponseEntity.ok(credorService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CredorResponse> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(credorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CredorResponse> atualizar(
            @PathVariable String id,
            @Valid @RequestBody CredorRequest request) {
        return ResponseEntity.ok(credorService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable String id) {
        credorService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
