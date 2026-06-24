package com.tcc.rj_system.service;

import java.util.Locale;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tcc.rj_system.dto.AuthResponse;
import com.tcc.rj_system.dto.LoginRequest;
import com.tcc.rj_system.dto.RegisterRequest;
import com.tcc.rj_system.dto.UsuarioResponse;
import com.tcc.rj_system.exception.BusinessException;
import com.tcc.rj_system.model.Usuario;
import com.tcc.rj_system.repository.UsuarioRepository;
import com.tcc.rj_system.security.CustomUserDetails;
import com.tcc.rj_system.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_ROLE = "ADMIN";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("Já existe um usuário cadastrado com este e-mail");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(email)
                .senha(passwordEncoder.encode(request.senha()))
                .role(DEFAULT_ROLE)
                .build();

        Usuario savedUser = usuarioRepository.save(usuario);
        return buildAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos"));

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new BadCredentialsException("E-mail ou senha inválidos");
        }

        return buildAuthResponse(usuario);
    }

    public UsuarioResponse toResponse(CustomUserDetails principal) {
        if (principal == null) {
            throw new BadCredentialsException("Usuário autenticado inválido");
        }

        return mapToResponse(principal.getUsuario());
    }

    private AuthResponse buildAuthResponse(Usuario usuario) {
        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                "Bearer",
                token,
                jwtService.getExpirationMs() / 1000,
                mapToResponse(usuario));
    }

    private UsuarioResponse mapToResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
