package com.tcc.rj_system.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.tcc.rj_system.dto.CredorImportRequest;
import com.tcc.rj_system.dto.CredorRequest;
import com.tcc.rj_system.enums.StatusCredor;
import com.tcc.rj_system.enums.TipoCredor;
import com.tcc.rj_system.exception.ResourceNotFoundException;
import com.tcc.rj_system.model.Credor;
import com.tcc.rj_system.model.Processo;
import com.tcc.rj_system.repository.CredorRepository;
import com.tcc.rj_system.repository.ProcessoRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CredorServiceTest {
    private CredorRequest item(String processoId) {
        return new CredorRequest("Credor", "12345678901", processoId, TipoCredor.QUIROGRAFARIO,
                new BigDecimal("100.00"), StatusCredor.HABILITADO);
    }

    @Test
    void validaTodosOsProcessosAntesDeSalvar() {
        var credores = mock(CredorRepository.class);
        var processos = mock(ProcessoRepository.class);
        when(processos.findById("p1")).thenReturn(Optional.of(Processo.builder().id("p1").build()));
        when(processos.findById("inexistente")).thenReturn(Optional.empty());
        var service = new CredorService(credores, processos);
        assertThrows(ResourceNotFoundException.class, () -> service.importar(
                new CredorImportRequest(List.of(item("p1"), item("inexistente")))));
        verifyNoInteractions(credores);
        verify(processos, never()).save(any());
    }

    @Test
    void repetirLoteNaoDuplicaCredoresNemVinculos() {
        var credores = mock(CredorRepository.class);
        var processos = mock(ProcessoRepository.class);
        var processo = Processo.builder().id("p1").build();
        var salvos = new HashMap<String, Credor>();
        when(processos.findById("p1")).thenReturn(Optional.of(processo));
        when(credores.findById(anyString())).thenAnswer(i -> Optional.ofNullable(salvos.get(i.getArgument(0))));
        when(credores.save(any())).thenAnswer(i -> {
            Credor credor = i.getArgument(0);
            salvos.put(credor.getId(), credor);
            return credor;
        });
        var service = new CredorService(credores, processos);
        var lote = new CredorImportRequest(List.of(item("p1")));
        var first = service.importar(lote);
        var retry = service.importar(lote);
        assertEquals(first.credores().getFirst().id(), retry.credores().getFirst().id());
        assertEquals(1, salvos.size());
        assertEquals(1, processo.getCredores().size());
        verify(credores, times(1)).save(any());
    }

    @Test
    void repeticaoReparaVinculoAposFalhaAoSalvarProcesso() {
        var credores = mock(CredorRepository.class);
        var processos = mock(ProcessoRepository.class);
        var primeiroProcesso = Processo.builder().id("p1").build();
        var processoRecarregado = Processo.builder().id("p1").build();
        var salvos = new HashMap<String, Credor>();
        when(processos.findById("p1")).thenReturn(Optional.of(primeiroProcesso), Optional.of(processoRecarregado));
        when(credores.findById(anyString())).thenAnswer(i -> Optional.ofNullable(salvos.get(i.getArgument(0))));
        when(credores.save(any())).thenAnswer(i -> {
            Credor credor = i.getArgument(0);
            salvos.put(credor.getId(), credor);
            return credor;
        });
        when(processos.save(any())).thenThrow(new RuntimeException("Banco indisponivel"))
                .thenReturn(processoRecarregado);
        var service = new CredorService(credores, processos);
        var lote = new CredorImportRequest(List.of(item("p1")));
        assertThrows(RuntimeException.class, () -> service.importar(lote));
        service.importar(lote);
        assertEquals(1, salvos.size());
        assertEquals(1, processoRecarregado.getCredores().size());
        verify(credores, times(1)).save(any());
    }
}
