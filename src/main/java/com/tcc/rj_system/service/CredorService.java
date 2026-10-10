package com.tcc.rj_system.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.tcc.rj_system.dto.CredorImportRequest;
import com.tcc.rj_system.dto.CredorImportResponse;
import com.tcc.rj_system.dto.CredorRequest;
import com.tcc.rj_system.dto.CredorResponse;
import com.tcc.rj_system.exception.ResourceNotFoundException;
import com.tcc.rj_system.exception.BusinessException;
import com.tcc.rj_system.model.Credor;
import com.tcc.rj_system.model.Processo;
import com.tcc.rj_system.repository.CredorRepository;
import com.tcc.rj_system.repository.ProcessoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CredorService {

    private final CredorRepository credorRepository;
    private final ProcessoRepository processoRepository;

    public CredorResponse criar(CredorRequest request) {
        Processo processo = buscarProcesso(request.processoId());
        Credor credor = credorRepository.save(toEntity(request, processo));
        vincularCredorAoProcesso(processo, credor);
        return toResponse(credor);
    }

    public CredorImportResponse importar(CredorImportRequest request) {
        // Validate all referenced processes before the first write.
        Map<String, Processo> processos = new HashMap<>();
        request.credores().forEach(item -> processos.computeIfAbsent(item.processoId(), this::buscarProcesso));
        List<CredorResponse> credoresImportados = request.credores()
                .stream()
                .map(item -> {
                    Processo processo = processos.get(item.processoId());
                    // Stable IDs make retries of an identical import safe, including after a failed link write.
                    String key = String.join("\u001f", item.processoId(), item.documento(), item.nome(),
                            item.tipo().name(), item.valor().stripTrailingZeros().toPlainString(), item.status().name());
                    String id = "import-" + UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
                    Credor credor = credorRepository.findById(id).orElseGet(() -> {
                        Credor novo = toEntity(item, processo);
                        novo.setId(id);
                        return credorRepository.save(novo);
                    });
                    if (credor.getProcesso() == null || !Objects.equals(credor.getProcesso().getId(), processo.getId())) {
                        throw new BusinessException("Um credor deste lote foi transferido ou perdeu seu processo. Revise o cadastro antes de repetir a importacao.");
                    }
                    vincularCredorAoProcesso(processo, credor);
                    return toResponse(credor);
                })
                .toList();

        return new CredorImportResponse(credoresImportados.size(), credoresImportados);
    }

    public List<CredorResponse> listar() {
        return credorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CredorResponse> listarPorProcesso(String processoId) {
        Processo processo = buscarProcesso(processoId);
        return credorRepository.findByProcesso(processo)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CredorResponse buscarPorId(String id) {
        return toResponse(buscarEntidade(id));
    }

    public CredorResponse atualizar(String id, CredorRequest request) {
        Credor credor = buscarEntidade(id);
        Processo processoAnterior = credor.getProcesso();
        Processo processo = buscarProcesso(request.processoId());

        credor.setNome(request.nome());
        credor.setDocumento(request.documento());
        credor.setProcesso(processo);
        credor.setTipo(request.tipo());
        credor.setValor(request.valor());
        credor.setStatus(request.status());

        Credor credorAtualizado = credorRepository.save(credor);
        atualizarVinculoComProcesso(processoAnterior, processo, credorAtualizado);
        return toResponse(credorAtualizado);
    }

    public void remover(String id) {
        Credor credor = buscarEntidade(id);
        removerCredorDoProcesso(credor.getProcesso(), credor);
        credorRepository.delete(credor);
    }

    private Credor buscarEntidade(String id) {
        return credorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credor nao encontrado para o id: " + id));
    }

    private Processo buscarProcesso(String processoId) {
        return processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException("Processo nao encontrado para o id: " + processoId));
    }

    private void atualizarVinculoComProcesso(Processo processoAnterior, Processo processoAtual, Credor credor) {
        if (processoAnterior != null && !Objects.equals(processoAnterior.getId(), processoAtual.getId())) {
            removerCredorDoProcesso(processoAnterior, credor);
        }

        vincularCredorAoProcesso(processoAtual, credor);
    }

    private void vincularCredorAoProcesso(Processo processo, Credor credor) {
        if (processo.getCredores() == null) {
            processo.setCredores(new ArrayList<>());
        }

        boolean credorJaVinculado = processo.getCredores()
                .stream()
                .anyMatch(credorDoProcesso -> Objects.equals(credorDoProcesso.getId(), credor.getId()));

        if (!credorJaVinculado) {
            processo.getCredores().add(credor);
            processoRepository.save(processo);
        }
    }

    private void removerCredorDoProcesso(Processo processo, Credor credor) {
        if (processo == null || processo.getCredores() == null) {
            return;
        }

        boolean removeuCredor = processo.getCredores()
                .removeIf(credorDoProcesso -> Objects.equals(credorDoProcesso.getId(), credor.getId()));

        if (removeuCredor) {
            processoRepository.save(processo);
        }
    }

    private Credor toEntity(CredorRequest request, Processo processo) {
        return Credor.builder()
                .nome(request.nome())
                .documento(request.documento())
                .processo(processo)
                .tipo(request.tipo())
                .valor(request.valor())
                .status(request.status())
                .dataCadastro(Instant.now())
                .build();
    }

    private CredorResponse toResponse(Credor credor) {
        return new CredorResponse(
                credor.getId(),
                credor.getNome(),
                credor.getDocumento(),
                credor.getProcesso().getId(),
                credor.getProcesso().getNumeroProcesso(),
                credor.getProcesso().getEmpresa(),
                credor.getTipo(),
                credor.getTipo().getClasse(),
                credor.getTipo().getDescricao(),
                credor.getValor(),
                credor.getStatus(),
                credor.getDataCadastro());
    }
}
