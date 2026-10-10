package com.tcc.rj_system.service;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.tcc.rj_system.exception.BusinessException;
import com.tcc.rj_system.model.Processo;
import com.tcc.rj_system.repository.CredorRepository;
import com.tcc.rj_system.repository.ProcessoRepository;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProcessoServiceTest {
    @Test
    void naoRemoveProcessoNemDocumentosQuandoHaCredores() {
        var processos = mock(ProcessoRepository.class);
        var credores = mock(CredorRepository.class);
        var documentos = mock(DocumentoProcessoService.class);
        var processo = Processo.builder().id("p1").build();
        when(processos.findById("p1")).thenReturn(Optional.of(processo));
        when(credores.existsByProcesso(processo)).thenReturn(true);
        var service = new ProcessoService(processos, credores, documentos);
        assertThrows(BusinessException.class, () -> service.remover("p1"));
        verify(processos, never()).delete(any());
        verifyNoInteractions(documentos);
    }

    @Test
    void removeProcessoSemCredores() {
        var processos = mock(ProcessoRepository.class);
        var credores = mock(CredorRepository.class);
        var documentos = mock(DocumentoProcessoService.class);
        var processo = Processo.builder().id("p1").build();
        when(processos.findById("p1")).thenReturn(Optional.of(processo));
        new ProcessoService(processos, credores, documentos).remover("p1");
        verify(documentos).removerDocumentosDoProcesso("p1");
        verify(processos).delete(processo);
    }
}
