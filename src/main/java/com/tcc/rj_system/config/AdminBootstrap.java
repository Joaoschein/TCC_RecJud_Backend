package com.tcc.rj_system.config;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.tcc.rj_system.model.Usuario;
import com.tcc.rj_system.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

/** Optional first administrator setup, supplied through environment variables. */
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.email:}")
    private String email;
    @Value("${app.bootstrap.admin.password:}")
    private String password;

    @Override
    public void run(String... args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.length() < 8) {
            throw new IllegalArgumentException("Informe e-mail e senha de pelo menos 8 caracteres para o administrador inicial.");
        }
        // Never promote or overwrite an existing user.
        if (usuarioRepository.count() != 0) return;
        usuarioRepository.save(Usuario.builder()
                .nome("Administrador")
                .email(email.trim().toLowerCase(Locale.ROOT))
                .senha(passwordEncoder.encode(password))
                .role("ADMIN")
                .build());
    }
}
