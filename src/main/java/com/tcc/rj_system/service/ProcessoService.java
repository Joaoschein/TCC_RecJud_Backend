package com.tcc.rj_system.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tcc.rj_system.dto.CredorResumoResponse;
import com.tcc.rj_system.dto.ProcessoRequest;
import com.tcc.rj_system.dto.ProcessoResponse;
import com.tcc.rj_system.exception.ResourceNotFoundException;
import com.tcc.rj_system.model.Credor;
import com.tcc.rj_system.model.Processo;
import com.tcc.rj_system.repository.ProcessoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessoService {

    private final ProcessoRepository processoRepository;

    public ProcessoResponse criar(ProcessoRequest request) {
        Processo processo = processoRepository.save(toEntity(request));
        return toResponse(processo);
    }

    public List<ProcessoResponse> listar() {
        return processoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProcessoResponse buscarPorId(String id) {
        return toResponse(buscarEntidade(id));
    }

    public ProcessoResponse atualizar(String id, ProcessoRequest request) {
        Processo processo = buscarEntidade(id);
        processo.setNumeroProcesso(request.numeroProcesso());
        processo.setEmpresa(request.empresa());
        processo.setCnpj(request.cnpj());
        processo.setVara(request.vara());
        processo.setAbertura(request.abertura());
        processo.setValorTotal(request.valorTotal());
        processo.setStatus(request.status());

        return toResponse(processoRepository.save(processo));
    }

    public void remover(String id) {
        Processo processo = buscarEntidade(id);
        processoRepository.delete(processo);
    }

    private Processo buscarEntidade(String id) {
        return processoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Processo nao encontrado para o id: " + id));
    }

    private Processo toEntity(ProcessoRequest request) {
        return Processo.builder()
                .numeroProcesso(request.numeroProcesso())
                .empresa(request.empresa())
                .cnpj(request.cnpj())
                .vara(request.vara())
                .abertura(request.abertura())
                .valorTotal(request.valorTotal())
                .status(request.status())
                .build();
    }

    private ProcessoResponse toResponse(Processo processo) {
        return new ProcessoResponse(
                processo.getId(),
                processo.getNumeroProcesso(),
                processo.getEmpresa(),
                processo.getCnpj(),
                processo.getVara(),
                processo.getAbertura(),
                processo.getValorTotal(),
                processo.getStatus(),
                toCredoresResponse(processo.getCredores()));
    }

    private List<CredorResumoResponse> toCredoresResponse(List<Credor> credores) {
        if (credores == null) {
            return List.of();
        }

        return credores.stream()
                .map(this::toCredorResumoResponse)
                .toList();
    }

    private CredorResumoResponse toCredorResumoResponse(Credor credor) {
        return new CredorResumoResponse(
                credor.getId(),
                credor.getNome(),
                credor.getDocumento(),
                credor.getTipo(),
                credor.getTipo().getClasse(),
                credor.getTipo().getDescricao(),
                credor.getValor(),
                credor.getStatus(),
                credor.getDataCadastro());
    }
}
