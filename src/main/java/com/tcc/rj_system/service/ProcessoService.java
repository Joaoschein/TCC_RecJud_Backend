package com.tcc.rj_system.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tcc.rj_system.dto.CredorResumoResponse;
import com.tcc.rj_system.dto.EventoTimelineRequest;
import com.tcc.rj_system.dto.EventoTimelineResponse;
import com.tcc.rj_system.dto.ProcessoRequest;
import com.tcc.rj_system.dto.ProcessoResponse;
import com.tcc.rj_system.exception.ResourceNotFoundException;
import com.tcc.rj_system.exception.ValidationException;
import com.tcc.rj_system.model.Credor;
import com.tcc.rj_system.model.EventoTimeline;
import com.tcc.rj_system.model.Processo;
import com.tcc.rj_system.repository.ProcessoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessoService {

    private final ProcessoRepository processoRepository;
    private final DocumentoProcessoService documentoProcessoService;

    public ProcessoResponse criar(ProcessoRequest request) {
        validarProcessoRequest(request);
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
        validarProcessoRequest(request);
        Processo processo = buscarEntidade(id);
        processo.setNumeroProcesso(request.numeroProcesso());
        processo.setEmpresa(request.empresa());
        processo.setCnpj(request.cnpj());
        processo.setVara(request.vara());
        if (request.juizResponsavel() != null) {
            processo.setJuizResponsavel(request.juizResponsavel());
        }
        if (request.comarca() != null) {
            processo.setComarca(request.comarca());
        }
        if (request.administradorJudicial() != null) {
            processo.setAdministradorJudicial(request.administradorJudicial());
        }
        processo.setAbertura(request.abertura());
        processo.setStatus(request.status());
        if (request.timeline() != null) {
            processo.setTimeline(toEventosTimeline(request.timeline()));
        }

        return toResponse(processoRepository.save(processo));
    }

    public ProcessoResponse atualizarTimeline(String id, List<EventoTimelineRequest> timeline) {
        Processo processo = buscarEntidade(id);
        processo.setTimeline(toEventosTimeline(timeline));
        return toResponse(processoRepository.save(processo));
    }

    public void remover(String id) {
        Processo processo = buscarEntidade(id);
        documentoProcessoService.removerDocumentosDoProcesso(id);
        processoRepository.delete(processo);
    }

    private Processo buscarEntidade(String id) {
        return processoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Processo nao encontrado para o id: " + id));
    }

    private void validarProcessoRequest(ProcessoRequest request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            throw new ValidationException(List.of("processo: O corpo da requisicao e obrigatorio"));
        }

        if (isBlank(request.numeroProcesso())) {
            errors.add("numeroProcesso: O numero do processo e obrigatorio");
        } else if (digitsOnly(request.numeroProcesso()).length() < 20) {
            errors.add("numeroProcesso: numero de processo invalido");
        }

        if (isBlank(request.empresa())) {
            errors.add("empresa: O nome da empresa e obrigatorio");
        } else if (request.empresa().trim().length() > 150) {
            errors.add("empresa: O nome da empresa deve ter no maximo 150 caracteres");
        }

        if (isBlank(request.cnpj())) {
            errors.add("cnpj: O CNPJ da empresa e obrigatorio");
        } else if (digitsOnly(request.cnpj()).length() != 14) {
            errors.add("cnpj: numero de CNPJ invalido");
        }

        if (request.vara() != null && request.vara().trim().length() > 120) {
            errors.add("vara: A vara responsavel deve ter no maximo 120 caracteres");
        }

        if (request.juizResponsavel() != null && request.juizResponsavel().trim().length() > 150) {
            errors.add("juizResponsavel: O juiz responsavel deve ter no maximo 150 caracteres");
        }

        if (request.comarca() != null && request.comarca().trim().length() > 120) {
            errors.add("comarca: A comarca deve ter no maximo 120 caracteres");
        }

        if (request.administradorJudicial() != null && request.administradorJudicial().trim().length() > 150) {
            errors.add("administradorJudicial: O administrador judicial deve ter no maximo 150 caracteres");
        }

        if (request.abertura() == null) {
            errors.add("abertura: A data de abertura do processo e obrigatoria");
        }

        if (request.status() == null) {
            errors.add("status: O status do processo e obrigatorio");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private Processo toEntity(ProcessoRequest request) {
        return Processo.builder()
                .numeroProcesso(request.numeroProcesso())
                .empresa(request.empresa())
                .cnpj(request.cnpj())
                .vara(request.vara())
                .juizResponsavel(request.juizResponsavel())
                .comarca(request.comarca())
                .administradorJudicial(request.administradorJudicial())
                .abertura(request.abertura())
                .valorTotal(BigDecimal.ZERO)
                .status(request.status())
                .timeline(toEventosTimeline(request.timeline()))
                .build();
    }

    private ProcessoResponse toResponse(Processo processo) {
        return new ProcessoResponse(
                processo.getId(),
                processo.getNumeroProcesso(),
                processo.getEmpresa(),
                processo.getCnpj(),
                processo.getVara(),
                processo.getJuizResponsavel(),
                processo.getComarca(),
                processo.getAdministradorJudicial(),
                processo.getAbertura(),
                calcularPassivoSujeito(processo.getCredores()),
                processo.getStatus(),
                toCredoresResponse(processo.getCredores()),
                toTimelineResponse(processo.getTimeline()));
    }

    private List<CredorResumoResponse> toCredoresResponse(List<Credor> credores) {
        if (credores == null) {
            return List.of();
        }

        return credores.stream()
                .map(this::toCredorResumoResponse)
                .toList();
    }

    private BigDecimal calcularPassivoSujeito(List<Credor> credores) {
        if (credores == null) {
            return BigDecimal.ZERO;
        }

        return credores.stream()
                .map(Credor::getValor)
                .filter(valor -> valor != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    private List<EventoTimeline> toEventosTimeline(List<EventoTimelineRequest> eventos) {
        if (eventos == null) {
            return List.of();
        }

        return eventos.stream()
                .map(this::toEventoTimeline)
                .toList();
    }

    private EventoTimeline toEventoTimeline(EventoTimelineRequest evento) {
        return EventoTimeline.builder()
                .id(evento.id() == null || evento.id().isBlank() ? UUID.randomUUID().toString() : evento.id())
                .titulo(evento.titulo())
                .descricao(evento.descricao())
                .data(evento.data())
                .status(evento.status())
                .build();
    }

    private List<EventoTimelineResponse> toTimelineResponse(List<EventoTimeline> eventos) {
        if (eventos == null) {
            return List.of();
        }

        return eventos.stream()
                .map(this::toEventoTimelineResponse)
                .toList();
    }

    private EventoTimelineResponse toEventoTimelineResponse(EventoTimeline evento) {
        return new EventoTimelineResponse(
                evento.getId(),
                evento.getTitulo(),
                evento.getDescricao(),
                evento.getData(),
                evento.getStatus());
    }
}
