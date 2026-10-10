package com.tcc.rj_system.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tcc.rj_system.dto.DocumentoProcessoResponse;
import com.tcc.rj_system.service.DocumentoProcessoService;
import com.tcc.rj_system.service.DocumentoProcessoService.DocumentoDownload;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DocumentoProcessoController {

    private final DocumentoProcessoService documentoProcessoService;

    @PostMapping(path = "/processos/{processoId}/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoProcessoResponse> upload(
            @PathVariable String processoId,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("nome") String nome,
            @RequestParam(value = "tipo", required = false) String tipo) {
        DocumentoProcessoResponse response = documentoProcessoService.salvar(processoId, nome, tipo, arquivo);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/documentos/{id}/download")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/processos/{processoId}/documentos")
    public ResponseEntity<List<DocumentoProcessoResponse>> listar(@PathVariable String processoId) {
        return ResponseEntity.ok(documentoProcessoService.listarPorProcesso(processoId));
    }

    @GetMapping("/documentos/{id}/download")
    public ResponseEntity<?> download(@PathVariable String id) {
        DocumentoDownload download = documentoProcessoService.buscarParaDownload(id);
        String contentType = download.documento().getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : download.documento().getContentType();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(download.documento().getTamanhoBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.documento().getNomeArquivo())
                        .build()
                .toString())
                .body(download.resource());
    }

    @DeleteMapping("/documentos/{id}")
    public ResponseEntity<Void> remover(@PathVariable String id) {
        documentoProcessoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
