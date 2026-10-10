package com.tcc.rj_system.service;

import org.junit.jupiter.api.Test;
import com.tcc.rj_system.dto.RegisterRequest;
import com.tcc.rj_system.model.Usuario;
import com.tcc.rj_system.repository.UsuarioRepository;
import com.tcc.rj_system.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    @Test
    void novoCadastroNaoRecebePermissaoAdministrativa() {
        var usuarios = mock(UsuarioRepository.class);
        var encoder = mock(PasswordEncoder.class);
        var jwt = mock(JwtService.class);
        when(encoder.encode("senha123")).thenReturn("hash");
        when(usuarios.save(any())).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId("u1");
            return usuario;
        });
        var response = new AuthService(usuarios, encoder, jwt)
                .register(new RegisterRequest("Operador", " User@Example.com ", "senha123"));
        assertEquals("OPERADOR", response.usuario().role());
        assertEquals("user@example.com", response.usuario().email());
        verify(encoder).encode("senha123");
    }
}
