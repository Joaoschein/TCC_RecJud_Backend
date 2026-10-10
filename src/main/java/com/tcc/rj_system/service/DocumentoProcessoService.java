package com.tcc.rj_system.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.tcc.rj_system.dto.DocumentoProcessoResponse;
import com.tcc.rj_system.exception.BusinessException;
import com.tcc.rj_system.exception.ResourceNotFoundException;
import com.tcc.rj_system.model.DocumentoProcesso;
import com.tcc.rj_system.repository.DocumentoProcessoRepository;
import com.tcc.rj_system.repository.ProcessoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentoProcessoService {

    private static final long TAMANHO_MAXIMO_BYTES = 20 * 1024 * 1024;

    private static final Set<String> CONTENT_TYPES_PERMITIDOS = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/png");

    private final DocumentoProcessoRepository documentoProcessoRepository;
    private final ProcessoRepository processoRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public DocumentoProcessoResponse salvar(String processoId, String nome, String tipo, MultipartFile arquivo) {
        validarProcesso(processoId);
        validarArquivo(arquivo);

        String nomeArquivoOriginal = StringUtils.cleanPath(arquivo.getOriginalFilename() == null
                ? "documento"
                : arquivo.getOriginalFilename());
        String extensao = extrairExtensao(nomeArquivoOriginal);
        String nomeArquivoArmazenado = UUID.randomUUID() + extensao;
        Path diretorioProcesso = getDiretorioProcesso(processoId);
        Path destino = diretorioProcesso.resolve(nomeArquivoArmazenado).normalize();

        if (!destino.startsWith(diretorioProcesso)) {
            throw new BusinessException("Nome de arquivo invalido.");
        }

        try {
            Files.createDirectories(diretorioProcesso);
            try (InputStream inputStream = arquivo.getInputStream()) {
                Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new BusinessException("Nao foi possivel salvar o arquivo.");
        }

        DocumentoProcesso documento = DocumentoProcesso.builder()
                .processoId(processoId)
                .nome(normalizarNome(nome, nomeArquivoOriginal))
                .nomeArquivo(nomeArquivoOriginal)
                .contentType(arquivo.getContentType())
                .tamanhoBytes(arquivo.getSize())
                .tipo(normalizarTexto(tipo))
                .caminhoArquivo(destino.toString())
                .dataUpload(Instant.now())
                .build();

        return toResponse(documentoProcessoRepository.save(documento));
    }

    public List<DocumentoProcessoResponse> listarPorProcesso(String processoId) {
        validarProcesso(processoId);
        return documentoProcessoRepository.findByProcessoIdOrderByDataUploadDesc(processoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DocumentoDownload buscarParaDownload(String id) {
        DocumentoProcesso documento = buscarEntidade(id);
        Path caminho = Paths.get(documento.getCaminhoArquivo()).normalize();

        try {
            Resource resource = new UrlResource(caminho.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Arquivo nao encontrado para o documento: " + id);
            }

            return new DocumentoDownload(documento, resource);
        } catch (IOException exception) {
            throw new ResourceNotFoundException("Arquivo nao encontrado para o documento: " + id);
        }
    }

    public void remover(String id) {
        DocumentoProcesso documento = buscarEntidade(id);
        removerArquivo(documento);
        documentoProcessoRepository.delete(documento);
    }

    public void removerDocumentosDoProcesso(String processoId) {
        List<DocumentoProcesso> documentos = documentoProcessoRepository.findByProcessoIdOrderByDataUploadDesc(processoId);
        documentos.forEach(this::removerArquivo);
        documentoProcessoRepository.deleteByProcessoId(processoId);
    }

    private DocumentoProcesso buscarEntidade(String id) {
        return documentoProcessoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento nao encontrado para o id: " + id));
    }

    private void validarProcesso(String processoId) {
        if (!processoRepository.existsById(processoId)) {
            throw new ResourceNotFoundException("Processo nao encontrado para o id: " + processoId);
        }
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessException("Envie um arquivo para upload.");
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new BusinessException("O arquivo deve ter no maximo 20 MB.");
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || !CONTENT_TYPES_PERMITIDOS.contains(contentType)) {
            throw new BusinessException("Tipo de arquivo nao permitido.");
        }
    }

    private String normalizarNome(String nome, String nomeArquivoOriginal) {
        String nomeDocumento = normalizarTexto(nome);
        return nomeDocumento == null ? nomeArquivoOriginal : nomeDocumento;
    }

    private String normalizarTexto(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private String extrairExtensao(String nomeArquivo) {
        int index = nomeArquivo.lastIndexOf('.');
        if (index < 0 || index == nomeArquivo.length() - 1) {
            return "";
        }

        return nomeArquivo.substring(index);
    }

    private Path getDiretorioProcesso(String processoId) {
        return Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize()
                .resolve("processos")
                .resolve(processoId)
                .normalize();
    }

    private void removerArquivo(DocumentoProcesso documento) {
        try {
            Files.deleteIfExists(Paths.get(documento.getCaminhoArquivo()));
        } catch (IOException ignored) {
            // A remocao do processo nao deve falhar caso o arquivo ja tenha sido removido manualmente.
        }
    }

    private DocumentoProcessoResponse toResponse(DocumentoProcesso documento) {
        return new DocumentoProcessoResponse(
                documento.getId(),
                documento.getProcessoId(),
                documento.getNome(),
                documento.getNomeArquivo(),
                documento.getContentType(),
                documento.getTamanhoBytes(),
                documento.getTipo(),
                documento.getDataUpload());
    }

    public record DocumentoDownload(DocumentoProcesso documento, Resource resource) {
    }
}
